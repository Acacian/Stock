package stock.stock_service.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import stock.stock_service.kafka.StockPriceProducer;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;
import stock.stock_service.service.StockPriceService;
import stock.stock_service.service.StockService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class StockPriceScheduler {

    private static final int YEARS_OF_DATA = 5;
    private static final int DAYS_PER_YEAR = 365;

    @Autowired
    private StockPriceProducer stockPriceProducer;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockPriceService stockPriceService;

    @Scheduled(cron = "${stock.scheduler.daily.cron:0 0 18 * * MON-FRI}")
    public void updateDailyStockPrices() {
        updateStockPrices(1);
    }

    @Scheduled(cron = "${stock.scheduler.init.cron:0 0 1 * * ?}")
    public void initializeHistoricalData() {
        updateStockPrices(YEARS_OF_DATA * DAYS_PER_YEAR);
    }

    private void updateStockPrices(int days) {
        List<Stock> allStocks = stockService.getAllStocksToUpdate();
        
        allStocks.forEach(stock -> 
            CompletableFuture.runAsync(() -> {
                stockPriceService.fetchAndSaveStockPrices(stock, days);
                List<StockPrice> prices = stockPriceService.getStockPrices(stock, stock.getLastUpdated(), java.time.LocalDate.now());
                prices.forEach(stockPriceProducer::sendStockPrice);
            })
        );
    }
}