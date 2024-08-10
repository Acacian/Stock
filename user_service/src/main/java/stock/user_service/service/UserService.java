package stock.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import stock.user_service.model.User;
// import stock.user_service.model.Post;
// import stock.user_service.model.Comment;
// import stock.user_service.model.Follow;
// import stock.user_service.model.Like;
import stock.user_service.repository.*;
import stock.user_service.dto.UpdateProfileRequest;
import stock.common.event.UserEvent;

import java.time.LocalDateTime;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private PostRepository postRepository;

    // @Autowired
    // private CommentRepository commentRepository;

    // @Autowired
    // private FollowRepository followRepository;

    // @Autowired
    // private LikeRepository likeRepository;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(Long id, UpdateProfileRequest request) {
        User user = getUser(id);
        user.setName(request.getName());
        user.setProfileImage(request.getProfileImage());
        user.setIntroduction(request.getIntroduction());
        User updatedUser = userRepository.save(user);
        
        kafkaTemplate.send("user-events", new UserEvent("PROFILE_UPDATED", id, null));
        return updatedUser;
    }

    public void processAuthenticatedUser(Long userId) {
        logger.info("Processing authenticated user with ID: {}", userId);
        User user = getUser(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("Updated last login time for user ID: {}", userId);
    }

    // public void followUser(Long followerId, Long followeeId) {
    //     User follower = getUser(followerId);
    //     User followee = getUser(followeeId);

    //     if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
    //         throw new RuntimeException("Already following this user");
    //     }

    //     Follow follow = new Follow();
    //     follow.setFollower(follower);
    //     follow.setFollowee(followee);
    //     follow.setCreatedAt(LocalDateTime.now());
    //     followRepository.save(follow);

    //     kafkaTemplate.send("user-events", new UserEvent("USER_FOLLOWED", followerId, followeeId.toString()));
    // }

    // public void createPost(Long userId, String content) {
    //     User user = getUser(userId);

    //     Post post = new Post();
    //     post.setUser(user);
    //     post.setContent(content);
    //     post.setCreatedAt(LocalDateTime.now());
    //     postRepository.save(post);

    //     kafkaTemplate.send("user-events", new UserEvent("POST_CREATED", userId, content));
    // }

    // public void addComment(Long userId, Long postId, String content) {
    //     User user = getUser(userId);
    //     Post post = postRepository.findById(postId)
    //             .orElseThrow(() -> new RuntimeException("Post not found"));

    //     Comment comment = new Comment();
    //     comment.setUser(user);
    //     comment.setPost(post);
    //     comment.setContent(content);
    //     comment.setCreatedAt(LocalDateTime.now());
    //     commentRepository.save(comment);

    //     kafkaTemplate.send("user-events", new UserEvent("COMMENT_ADDED", userId, postId + "," + content));
    // }

    // public void likePost(Long userId, Long postId) {
    //     User user = getUser(userId);
    //     Post post = postRepository.findById(postId)
    //             .orElseThrow(() -> new RuntimeException("Post not found"));

    //     if (likeRepository.existsByUserAndPost(user, post)) {
    //         throw new RuntimeException("Already liked this post");
    //     }

    //     Like like = new Like();
    //     like.setUser(user);
    //     like.setPost(post);
    //     like.setCreatedAt(LocalDateTime.now());
    //     likeRepository.save(like);

    //     kafkaTemplate.send("user-events", new UserEvent("POST_LIKED", userId, postId.toString()));
    // }
}