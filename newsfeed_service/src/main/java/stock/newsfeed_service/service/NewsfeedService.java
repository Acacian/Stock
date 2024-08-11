package stock.newsfeed_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import stock.common.event.SocialEvent;

import java.util.List;

@Service
public class NewsfeedService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "social-events", groupId = "newsfeed-service-group")
    public void consumeSocialEvent(SocialEvent event) {
        String key = "newsfeed:" + event.getUserId();
        redisTemplate.opsForList().leftPush(key, event.toString());
        redisTemplate.opsForList().trim(key, 0, 99);  // Keep only the latest 100 events
    }

    public List<String> getNewsfeed(Long userId) {
        String key = "newsfeed:" + userId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}