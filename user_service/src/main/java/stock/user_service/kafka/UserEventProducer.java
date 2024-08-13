package stock.user_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {
    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void sendUserEvent(UserEvent event) {
        kafkaTemplate.send("user-events", event);
    }
}