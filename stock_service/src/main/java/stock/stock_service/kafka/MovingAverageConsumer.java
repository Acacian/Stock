package stock.stock_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovingAverageConsumer {

    @KafkaListener(topics = "moving-averages", groupId = "moving-average-group")
    public void consume(ConsumerRecord<String, List<Double>> record) {
        String stockCode = record.key();
        List<Double> movingAverages = record.value();

        System.out.println("Stock: " + stockCode);
        System.out.println("12-day MA: " + movingAverages.get(0));
        System.out.println("20-day MA: " + movingAverages.get(1));
        System.out.println("26-day MA: " + movingAverages.get(2));
    }
}