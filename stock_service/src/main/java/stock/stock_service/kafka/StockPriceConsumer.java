package stock.stock_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import stock.stock_service.model.StockPrice;
import stock.stock_service.repository.StockPriceRepository;

@Service
public class StockPriceConsumer {

    private final StockPriceRepository stockPriceRepository;

    public StockPriceConsumer(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
    }

    @KafkaListener(topics = "stock-prices", groupId = "stock-price-group")
    public void consume(StockPrice stockPrice) {
        stockPriceRepository.save(stockPrice);
    }
}