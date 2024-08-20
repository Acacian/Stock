package stock.stock_service.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import stock.stock_service.model.Stock;
import stock.stock_service.service.StockPriceService;
import stock.stock_service.service.StockService;

import java.util.List;

@Component
public class StockPriceScheduler {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockPriceService stockPriceService;

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void updateDailyStockPrices() {
        List<Stock> stocksToUpdate = stockService.getStocksToUpdate();
        for (Stock stock : stocksToUpdate) {
            stockPriceService.fetchAndSaveStockPrices(stock, 1);
        }
    }

    @Scheduled(cron = "0 0 1 * * SUN")
    public void updateWeeklyStockPrices() {
        List<Stock> stocksToUpdate = stockService.getStocksToUpdate();
        for (Stock stock : stocksToUpdate) {
            stockPriceService.fetchAndSaveStockPrices(stock, 7);
        }
    }

    @Scheduled(cron = "0 0 2 1 * ?")
    public void updateMonthlyStockPrices() {
        List<Stock> stocksToUpdate = stockService.getStocksToUpdate();
        for (Stock stock : stocksToUpdate) {
            stockPriceService.fetchAndSaveStockPrices(stock, 30);
        }
    }
}