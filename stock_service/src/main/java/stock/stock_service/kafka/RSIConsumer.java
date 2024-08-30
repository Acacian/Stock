package stock.stock_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RSIConsumer {

    @KafkaListener(topics = "rsi", groupId = "rsi-group")
    public void consume(ConsumerRecord<String, List<Double>> record) {
        String stockCode = record.key();
        List<Double> rsiValues = record.value();

        System.out.println("Stock: " + stockCode);
        System.out.println("RSI: " + rsiValues.get(0));
    }
}