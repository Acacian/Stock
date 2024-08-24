package stock.stock_service.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import stock.stock_service.model.Stock;
import stock.stock_service.service.StockPriceService;
import stock.stock_service.service.StockService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class StockPriceScheduler {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockPriceService stockPriceService;

    @Scheduled(cron = "${stock.scheduler.daily.cron:0 0 18 * * MON-FRI}")
    public void updateDailyStockPrices() {
        updateStockPrices(1);
    }

    @Scheduled(cron = "${stock.scheduler.weekly.cron:0 0 1 * * SUN}")
    public void updateWeeklyStockPrices() {
        updateStockPrices(7);
    }

    @Scheduled(cron = "${stock.scheduler.monthly.cron:0 0 2 1 * ?}")
    public void updateMonthlyStockPrices() {
        updateStockPrices(30);
    }

    private void updateStockPrices(int days) {
        List<Stock> stocksToUpdate = stockService.getStocksToUpdate();
        List<CompletableFuture<Void>> futures = stocksToUpdate.stream()
            .map(stock -> CompletableFuture.runAsync(() -> 
                stockPriceService.fetchAndSaveStockPrices(stock, days)))
            .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}