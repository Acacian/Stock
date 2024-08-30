package stock.stock_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import stock.stock_service.model.StockPrice;

@Service
public class StockPriceProducer {
    @Autowired
    private KafkaTemplate<String, StockPrice> kafkaTemplate;

    public void sendStockPrice(StockPrice stockPrice) {
        kafkaTemplate.send("stock-prices", stockPrice.getStock().getCode(), stockPrice);
    }
}