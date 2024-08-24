package stock.newsfeed_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import stock.newsfeed_service.model.NewsfeedItem;
import stock.newsfeed_service.repository.UserRepository;
import stock.newsfeed_service.kafka.StockEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsfeedService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void addFollowActivity(Long userId, Long targetUserId) {
        String userName = userRepository.getUserName(userId);
        String followedUserName = userRepository.getUserName(targetUserId);
        NewsfeedItem item = new NewsfeedItem("FOLLOW", userId, userName, targetUserId, followedUserName, null);
        addActivityToNewsfeed(userId, item);
        addActivityToFollowers(userId, item);
    }

    public void addPostActivity(Long userId, Long postId) {
        String userName = userRepository.getUserName(userId);
        NewsfeedItem item = new NewsfeedItem("POST", userId, userName, postId, null, null);
        addActivityToNewsfeed(userId, item);
        addActivityToFollowers(userId, item);
    }

    public void addCommentActivity(Long userId, Long postId, Long commentId) {
        String userName = userRepository.getUserName(userId);
        NewsfeedItem item = new NewsfeedItem("COMMENT", userId, userName, postId, null, null);
        addActivityToNewsfeed(userId, item);
        addActivityToFollowers(userId, item);
        addActivityToPostOwner(postId, item);
    }

    public void addLikeActivity(Long userId, Long postId) {
        String userName = userRepository.getUserName(userId);
        NewsfeedItem item = new NewsfeedItem("LIKE", userId, userName, postId, null, null);
        addActivityToNewsfeed(userId, item);
        addActivityToFollowers(userId, item);
        addActivityToPostOwner(postId, item);
    }

    public void addStockPriceChangeActivity(StockEvent event) {
        NewsfeedItem item = new NewsfeedItem("STOCK_CHANGE", event.getStockCode(), null, null, event.getPrice(), event.getChangePercentage());
        Set<Long> allUserIds = new HashSet<>(userRepository.getAllUserIds());
        for (Long userId : allUserIds) {
            addActivityToNewsfeed(userId, item);
        }
    }

    private void addActivityToNewsfeed(Long userId, NewsfeedItem item) {
        String key = "newsfeed:" + userId;
        redisTemplate.opsForList().leftPush(key, serializeNewsfeedItem(item));
        redisTemplate.opsForList().trim(key, 0, 99);
    }

    private void addActivityToFollowers(Long userId, NewsfeedItem item) {
        Set<Long> followers = userRepository.getFollowers(userId);
        for (Long followerId : followers) {
            addActivityToNewsfeed(followerId, item);
        }
    }

    private void addActivityToPostOwner(Long postId, NewsfeedItem item) {
        Long postOwnerId = userRepository.getPostOwner(postId);
        addActivityToNewsfeed(postOwnerId, item);
    }

    public List<NewsfeedItem> getNewsfeed(Long userId) {
        String key = "newsfeed:" + userId;
        List<String> serializedItems = redisTemplate.opsForList().range(key, 0, 99);
        return serializedItems.stream()
                .map(this::deserializeNewsfeedItem)
                .collect(Collectors.toList());
    }

    private String serializeNewsfeedItem(NewsfeedItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing NewsfeedItem", e);
        }
    }

    private NewsfeedItem deserializeNewsfeedItem(String serializedItem) {
        try {
            return objectMapper.readValue(serializedItem, NewsfeedItem.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing NewsfeedItem", e);
        }
    }
}
