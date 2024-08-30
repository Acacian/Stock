package stock.stock_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MACDConsumer {

    @KafkaListener(topics = "macd", groupId = "macd-group")
    public void consume(ConsumerRecord<String, List<Double>> record) {
        String stockCode = record.key();
        List<Double> macdResult = record.value();

        System.out.println("Stock: " + stockCode);
        System.out.println("MACD Line: " + macdResult.get(0));
        System.out.println("Signal Line: " + macdResult.get(1));
        System.out.println("Histogram: " + macdResult.get(2));
    }
}