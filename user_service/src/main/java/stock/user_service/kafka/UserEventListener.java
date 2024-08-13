package stock.user_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import stock.user_service.kafka.UserEvent;
import stock.user_service.service.UserService;

@Component
public class UserEventListener {
    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @Autowired
    private UserService userService;

    @KafkaListener(topics = "user-events", groupId = "user-service-group")
    public void listen(UserEvent event) {
        logger.info("Received user event: {}", event.getType());
        if ("USER_AUTHENTICATED".equals(event.getType()) || "PASSWORD_UPDATED".equals(event.getType())) {
            userService.processAuthenticatedUser(event.getUserId());
        }
    }
}