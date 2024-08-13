package stock.newsfeed_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import stock.newsfeed_service.service.NewsfeedService;

@Component
public class NewsfeedEventListener {
    @Autowired
    private NewsfeedService newsfeedService;

    @KafkaListener(topics = {"user-events", "social-events"}, groupId = "newsfeed-service-group")
    public void listen(Object event) {
        if (event instanceof UserEvent) {
            handleUserEvent((UserEvent) event);
        } else if (event instanceof SocialEvent) {
            handleSocialEvent((SocialEvent) event);
        }
    }

    private void handleUserEvent(UserEvent event) {
        switch (event.getType()) {
            case "USER_FOLLOWED":
                newsfeedService.addFollowActivity(event.getUserId(), event.getTargetUserId());
                break;
            // 다른 사용자 이벤트 처리...
        }
    }

    private void handleSocialEvent(SocialEvent event) {
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
}