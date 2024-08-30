package stock.social_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import stock.social_service.model.Post;
import stock.social_service.model.Comment;
import stock.social_service.model.Follow;
import stock.social_service.repository.PostRepository;
import stock.social_service.repository.CommentRepository;
import stock.social_service.repository.FollowRepository;
import stock.social_service.kafka.SocialEvent;
import stock.social_service.client.NewsfeedServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Service
public class SocialService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private NewsfeedServiceClient newsfeedServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(SocialService.class);

    public Post createPost(Long userId, String title, String content, Long stockId) {
        try {
            Post post = new Post();
            post.setUserId(userId);
            post.setTitle(title);
            post.setContent(content);
            post.setStockId(stockId);
            Post savedPost = postRepository.save(post);
            
            newsfeedServiceClient.postCreated(new SocialEvent("POST_CREATED", userId, savedPost.getId(), null));
            return savedPost;
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Invalid post data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating post: ", e);
            throw new InternalServerErrorException("An error occurred while creating the post");
        }
    }

    public Comment addComment(Long userId, Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);

        post.getComments().add(savedComment);
        postRepository.save(post);

        newsfeedServiceClient.commentCreated(new SocialEvent("COMMENT_ADDED", userId, postId, savedComment.getId()));
        return savedComment;
    }

    public void likePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getLikes().add(userId)) {
            postRepository.save(post);
            newsfeedServiceClient.postLiked(new SocialEvent("POST_LIKED", userId, postId, null));
        }
    }

    public void unlikePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getLikes().remove(userId)) {
            postRepository.save(post);
            newsfeedServiceClient.postUnliked(new SocialEvent("POST_UNLIKED", userId, postId, null));
        }
    }

    public void follow(Long followerId, Long followeeId) {
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new RuntimeException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followRepository.save(follow);

        newsfeedServiceClient.userFollowed(new SocialEvent("USER_FOLLOWED", followerId, followeeId, null));
        createFollowerActivity(followerId, followeeId);
    }

    public void unfollow(Long followerId, Long followeeId) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
        newsfeedServiceClient.userUnfollowed(new SocialEvent("USER_UNFOLLOWED", followerId, followeeId, null));
    }

    public Page<Post> getPostsByUserId(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        Post post = getPostById(postId);
        return post.getComments();
    }

    public Page<Post> getPostsWithActivity(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        posts.getContent().forEach(post -> {
            post.setCommentCount(post.getComments().size());
            post.setLikeCount(post.getLikes().size());
        });
        return posts;
    }

    public void likeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getLikes().add(userId)) {
            commentRepository.save(comment);
            newsfeedServiceClient.commentLiked(new SocialEvent("COMMENT_LIKED", userId, comment.getPost().getId(), commentId));
        }
    }

    public void unlikeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getLikes().remove(userId)) {
            commentRepository.save(comment);
            newsfeedServiceClient.commentUnliked(new SocialEvent("COMMENT_UNLIKED", userId, comment.getPost().getId(), commentId));
        }
    }

    public List<SocialEvent> getFollowerActivity(Long userId) {
        List<Long> followers = followRepository.findFollowersByFolloweeId(userId);
        List<SocialEvent> followerActivities = new ArrayList<>();

        for (Long followerId : followers) {
            List<Post> followerPosts = postRepository.findRecentPostsByUserId(followerId);
            for (Post post : followerPosts) {
                followerActivities.add(new SocialEvent("FOLLOWER_POST", followerId, post.getId(), null));
            }
            
            List<Comment> followerComments = commentRepository.findRecentCommentsByUserId(followerId);
            for (Comment comment : followerComments) {
                followerActivities.add(new SocialEvent("FOLLOWER_COMMENT", followerId, comment.getPost().getId(), comment.getId()));
            }
        }

        followerActivities.sort(Comparator.comparing(SocialEvent::getTimestamp).reversed());

        return followerActivities;
    }

    private void createFollowerActivity(Long followerId, Long followeeId) {
        Post latestPost = postRepository.findTopByUserIdAndParentIsNullOrderByCreatedAtDesc(followeeId);
        if (latestPost != null) {
            newsfeedServiceClient.followerActivity(new SocialEvent("FOLLOWER_ACTIVITY", followerId, latestPost.getId(), null));
        }
    }

    public Page<Post> getPostsByStock(Long stockId, Pageable pageable) {
        return postRepository.findByStockId(stockId, pageable);
    }

    public Page<Post> searchPosts(String query, String searchType, Pageable pageable) {
        switch (searchType.toLowerCase()) {
            case "title":
                return postRepository.findByTitleContainingIgnoreCase(query, pageable);
            case "author":
                return postRepository.findByUserNameContainingIgnoreCase(query, pageable);
            case "content":
                return postRepository.findByContentContainingIgnoreCase(query, pageable);
            case "all":
                return postRepository.searchPosts(query, pageable);
            default:
                throw new IllegalArgumentException("Invalid search type: " + searchType);
        }
    }
}