package stock.newsfeed_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import stock.newsfeed_service.service.NewsfeedService;

@Component
public class NewsfeedEventListener {
    @Autowired
    private NewsfeedService newsfeedService;

    @KafkaListener(topics = "user-events", groupId = "newsfeed-service-group", 
                   containerFactory = "newsfeedKafkaListenerContainerFactory")
    public void listenUserEvents(UserEvent event) {
        switch (event.getType()) {
            case "USER_FOLLOWED":
                newsfeedService.addFollowActivity(event.getUserId(), event.getTargetUserId());
                break;
            // 다른 사용자 이벤트 처리...
        }
    }

    @KafkaListener(topics = "social-events", groupId = "newsfeed-service-group",
                   containerFactory = "newsfeedKafkaListenerContainerFactory")
    public void listenSocialEvents(SocialEvent event) {
        switch (event.getType()) {
            case "POST_CREATED":
                newsfeedService.addPostActivity(event.getUserId(), event.getPostId());
                break;
            case "COMMENT_CREATED":
                newsfeedService.addCommentActivity(event.getUserId(), event.getPostId(), event.getCommentId());
                break;
            case "POST_LIKED":
                newsfeedService.addLikeActivity(event.getUserId(), event.getPostId());
                break;
            // 다른 소셜 이벤트 처리...
        }
    }

    @KafkaListener(topics = "stock-events", groupId = "newsfeed-service-group", 
                   containerFactory = "newsfeedKafkaListenerContainerFactory")
    public void listenStockEvents(StockEvent event) {
        newsfeedService.addStockPriceChangeActivity(event);
    }
}