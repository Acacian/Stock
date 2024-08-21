package stock.stock_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import stock.stock_service.model.StockPrice;
import stock.stock_service.repository.StockPriceRepository;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class StockPriceConsumer {

    private final StockPriceRepository stockPriceRepository;
    private CountDownLatch latch = new CountDownLatch(1);

    public StockPriceConsumer(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
    }

    @KafkaListener(topics = "stock-prices", groupId = "stock-price-group", 
    containerFactory = "stockPriceKafkaListenerContainerFactory")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void consume(List<ConsumerRecord<String, StockPrice>> records, 
                        Acknowledgment acknowledgment,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) int offset) {
        try {
            for (ConsumerRecord<String, StockPrice> record : records) {
                StockPrice stockPrice = record.value();

                // 로그 컴팩션을 위해 키를 사용하여 기존 레코드를 업데이트하거나 새로 삽입
                stockPriceRepository.save(stockPrice);

                System.out.println("Consumed message: " + stockPrice + " from partition: " + partition + " at offset: " + offset);
            }
            // 모든 레코드 처리 후 오프셋 커밋
            acknowledgment.acknowledge();
            latch.countDown();
        } catch (Exception e) {
            System.err.println("Error processing messages: " + e.getMessage());
            // 예외 발생 시 재시도를 위해 예외를 던집니다.
            throw e;
        }
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}