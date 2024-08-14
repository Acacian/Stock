package stock.user_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import stock.user_service.service.UserService;

@Component
public class UserEventListener {
    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @Autowired
    private UserService userService;

    @KafkaListener(topics = "user-events", groupId = "user-service-group")
    public void listen(UserEvent event) {
        logger.info("Received user event: {}", event.getType());
        switch(event.getType()) {
            case "USER_AUTHENTICATED":
            case "PASSWORD_UPDATED":
                userService.processAuthenticatedUser(event.getUserId());
                break;
            // 필요한 경우 다른 이벤트 타입 처리 추가
        }
    }
}