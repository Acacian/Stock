package stock.user_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthEventProducer {
    @Autowired
    private KafkaTemplate<String, AuthEvent> authEventKafkaTemplate;

    public void sendAuthEvent(AuthEvent event) {
        authEventKafkaTemplate.send("user-events", event);
    }
}