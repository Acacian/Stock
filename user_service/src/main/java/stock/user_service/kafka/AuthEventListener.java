package stock.user_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import stock.user_service.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthEventListener.class);

    @Autowired
    private AuthService authService;

    @KafkaListener(topics = "user-events", groupId = "auth-service-group",  
                   containerFactory = "authEventKafkaListenerContainerFactory")
    public void listen(AuthEvent event) {
        try {
            switch(event.getType()) {
                case "USER_AUTHENTICATED":
                case "PASSWORD_UPDATED":
                    authService.processAuthenticatedUser(event.getUserId());
                    break;
                default:
                    logger.warn("Unknown event type: {}", event.getType());
            }
        } catch (Exception e) {
            logger.error("Error processing Kafka event", e);
        }
    }
}