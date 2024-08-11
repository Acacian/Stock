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
import stock.common.event.SocialEvent;

import java.util.List;
import java.util.stream.Collectors;

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
        
        kafkaTemplate.send("social-events", new SocialEvent("POST_CREATED", userId, savedPost.getId()));
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

        kafkaTemplate.send("social-events", new SocialEvent("COMMENT_ADDED", userId, postId));
        return savedComment;
    }

    public void likePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getLikes().add(userId)) {
            postRepository.save(post);
            kafkaTemplate.send("social-events", new SocialEvent("POST_LIKED", userId, postId));
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

        kafkaTemplate.send("social-events", new SocialEvent("USER_FOLLOWED", followerId, followedId));
    }

    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getComments().stream().collect(Collectors.toList());
    }
}
