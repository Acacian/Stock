package stock.newsfeed_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import stock.newsfeed_service.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Service
public class NewsfeedService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    public void addFollowActivity(Long userId, Long targetUserId) {
        String message = String.format("User %d followed User %d", userId, targetUserId);
        addActivityToNewsfeed(targetUserId, message);
    }

    public void addPostActivity(Long userId, Long postId) {
        String message = String.format("User %d created a new post: %d", userId, postId);
        addActivityToFollowers(userId, message);
    }

    public void addCommentActivity(Long userId, Long postId, Long commentId) {
        String message = String.format("User %d commented on post %d", userId, postId);
        addActivityToFollowers(userId, message);
        addActivityToPostOwner(postId, message);
    }

    public void addLikeActivity(Long userId, Long postId) {
        String message = String.format("User %d liked post %d", userId, postId);
        addActivityToFollowers(userId, message);
        addActivityToPostOwner(postId, message);
    }

    private void addActivityToFollowers(Long userId, String message) {
        Set<Long> followers = userRepository.getFollowers(userId);
        for (Long followerId : followers) {
            addActivityToNewsfeed(followerId, message);
        }
    }

    private void addActivityToPostOwner(Long postId, String message) {
        Long postOwnerId = userRepository.getPostOwner(postId);
        addActivityToNewsfeed(postOwnerId, message);
    }

    private void addActivityToNewsfeed(Long userId, String message) {
        String key = "newsfeed:" + userId;
        redisTemplate.opsForList().leftPush(key, message);
        redisTemplate.opsForList().trim(key, 0, 99);  // Keep only the latest 100 events
    }

    public List<String> getNewsfeed(Long userId) {
        String key = "newsfeed:" + userId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void clearAllNewsfeeds() {
        Set<String> keys = redisTemplate.keys("newsfeed:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }
}