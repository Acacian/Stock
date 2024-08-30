package stock.stock_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BollingerBandsConsumer {

    @KafkaListener(topics = "bollinger-bands", groupId = "bollinger-bands-group")
    public void consume(ConsumerRecord<String, List<Double>> record) {
        String stockCode = record.key();
        List<Double> bollingerBands = record.value();

        System.out.println("Stock: " + stockCode);
        System.out.println("Upper Band: " + bollingerBands.get(0));
        System.out.println("Middle Band: " + bollingerBands.get(1));
        System.out.println("Lower Band: " + bollingerBands.get(2));
    }
}