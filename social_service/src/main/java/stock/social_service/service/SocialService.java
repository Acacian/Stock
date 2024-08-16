package stock.social_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import stock.social_service.model.Post;
import stock.social_service.model.Comment;
import stock.social_service.model.Follow;
import stock.social_service.repository.PostRepository;
import stock.social_service.repository.CommentRepository;
import stock.social_service.repository.FollowRepository;
import stock.social_service.kafka.SocialEvent;

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
    private KafkaTemplate<String, SocialEvent> kafkaTemplate;

    public Post createPost(Long userId, String content) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        Post savedPost = postRepository.save(post);
        
        kafkaTemplate.send("social-events", new SocialEvent("POST_CREATED", userId, savedPost.getId(), null));
        return savedPost;
    }

    public Comment addComment(Long userId, Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPost(post);
        comment.setContent(content);
        Comment savedComment = commentRepository.save(comment);

        kafkaTemplate.send("social-events", new SocialEvent("COMMENT_ADDED", userId, postId, savedComment.getId()));
        return savedComment;
    }

    public void likePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getLikes().add(userId)) {
            postRepository.save(post);
            kafkaTemplate.send("social-events", new SocialEvent("POST_LIKED", userId, postId, null));
        }
    }

    public void unlikePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getLikes().remove(userId)) {
            postRepository.save(post);
            kafkaTemplate.send("social-events", new SocialEvent("POST_UNLIKED", userId, postId, null));
        }
    }

    public void follow(Long followerId, Long followedId) {
        if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw new RuntimeException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowedId(followedId);
        followRepository.save(follow);

        kafkaTemplate.send("social-events", new SocialEvent("USER_FOLLOWED", followerId, followedId, null));
        createFollowerActivity(followerId, followedId);
    }

    public void unfollow(Long followerId, Long followedId) {
        followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
        kafkaTemplate.send("social-events", new SocialEvent("USER_UNFOLLOWED", followerId, followedId, null));
    }

    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public List<Post> getPostsWithActivity(Long userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        for (Post post : posts) {
            long commentCount = commentRepository.countByPostId(post.getId());
            int likeCount = post.getLikes().size();
            post.setCommentCount(commentCount);
            post.setLikeCount(likeCount);
        }
        return posts;
    }

    public void likeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getLikes().add(userId)) {
            commentRepository.save(comment);
            kafkaTemplate.send("social-events", new SocialEvent("COMMENT_LIKED", userId, comment.getPost().getId(), commentId));
        }
    }

    public void unlikeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getLikes().remove(userId)) {
            commentRepository.save(comment);
            kafkaTemplate.send("social-events", new SocialEvent("COMMENT_UNLIKED", userId, comment.getPost().getId(), commentId));
        }
    }

    public List<SocialEvent> getFollowerActivity(Long userId) {
        List<Long> followers = followRepository.findFollowersByFollowedId(userId);
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

    private void createFollowerActivity(Long followerId, Long followedId) {
        Post latestPost = postRepository.findTopByUserIdOrderByCreatedAtDesc(followedId);
        if (latestPost != null) {
            kafkaTemplate.send("social-events", new SocialEvent("FOLLOWER_ACTIVITY", followerId, latestPost.getId(), null));
        }
    }
}