package stock.stock_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import stock.stock_service.model.StockPrice;
import stock.stock_service.repository.StockPriceRepository;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StockPriceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(StockPriceConsumer.class);
    private final StockPriceRepository stockPriceRepository;
    private CountDownLatch latch = new CountDownLatch(1);

    public StockPriceConsumer(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
    }

    @KafkaListener(topics = "stock-prices", groupId = "stock-price-group", 
                   containerFactory = "stockPriceKafkaListenerContainerFactory")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional
    public void consume(List<ConsumerRecord<String, StockPrice>> records, 
                        Acknowledgment acknowledgment) {
        try {
            for (ConsumerRecord<String, StockPrice> record : records) {
                StockPrice stockPrice = record.value();
                stockPriceRepository.save(stockPrice);
                logger.info("Consumed message: {} from partition: {} at offset: {}", 
                            stockPrice, record.partition(), record.offset());
            }
            acknowledgment.acknowledge();
            latch.countDown();
        } catch (Exception e) {
            logger.error("Error processing messages: ", e);
            // Consider sending failed messages to a dead letter queue
            throw e;
        }
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}