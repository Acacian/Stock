package stock.user_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthEventProducer {
    @Autowired
    private KafkaTemplate<String, AuthEvent> kafkaTemplate;

    public void sendAuthEvent(AuthEvent event) {
        kafkaTemplate.send("auth-events", event);
    }
}