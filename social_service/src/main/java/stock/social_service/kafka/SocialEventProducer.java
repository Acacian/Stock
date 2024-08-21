package stock.social_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SocialEventProducer {
    @Autowired
    private KafkaTemplate<String, SocialEvent> socialEventKafkaTemplate;

    public void sendSocialEvent(SocialEvent event) {
        socialEventKafkaTemplate.send("social-events", event);
    }
}