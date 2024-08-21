package stock.user_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import stock.user_service.service.AuthService;

@Component
public class AuthEventListener {

    @Autowired
    private AuthService authService;

    @KafkaListener(topics = "user-events", groupId = "auth-service-group",  containerFactory = "authEventKafkaListenerContainerFactory")
    public void listen(AuthEvent event) {
        switch(event.getType()) {
            case "USER_AUTHENTICATED":
            case "PASSWORD_UPDATED":
                authService.processAuthenticatedUser(event.getUserId());
                break;
            // 필요한 경우 다른 이벤트 타입 처리 추가
        }
    }
}