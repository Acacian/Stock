package stock.stock_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;
import stock.stock_service.repository.StockPriceRepository;
import stock.stock_service.repository.StockRepository;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockPriceService {

    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String NAVER_STOCK_API_URL = "https://fchart.stock.naver.com/sise.nhn?symbol={symbol}&timeframe=day&count={count}&requestType=0";

    @Transactional
    public void fetchAndSaveStockPrices(Stock stock, int count) {
        String apiResponse = restTemplate.getForObject(NAVER_STOCK_API_URL, String.class, stock.getCode(), count);
        List<StockPrice> stockPrices = parseApiResponse(apiResponse, stock);
        stockPriceRepository.saveAll(stockPrices);
    }

    private List<StockPrice> parseApiResponse(String apiResponse, Stock stock) {
        List<StockPrice> stockPrices = new ArrayList<>();
        String[] lines = apiResponse.split("\n");
        for (String line : lines) {
            if (line.startsWith("<item data=")) {
                String[] data = line.split("\\|");
                if (data.length == 6) {
                    StockPrice stockPrice = new StockPrice();
                    stockPrice.setStock(stock);
                    stockPrice.setDate(LocalDate.parse(data[0].substring(11), DateTimeFormatter.BASIC_ISO_DATE));
                    stockPrice.setOpenPrice(Integer.parseInt(data[1]));
                    stockPrice.setHighPrice(Integer.parseInt(data[2]));
                    stockPrice.setLowPrice(Integer.parseInt(data[3]));
                    stockPrice.setClosePrice(Integer.parseInt(data[4]));
                    stockPrice.setVolume(Long.parseLong(data[5].replace("\"/>", "")));
                    
                    // Calculate change amount and rate
                    StockPrice previousPrice = stockPriceRepository.findFirstByStockOrderByDateDesc(stock).orElse(null);
                    if (previousPrice != null) {
                        int changeAmount = stockPrice.getClosePrice() - previousPrice.getClosePrice();
                        double changeRate = (double) changeAmount / previousPrice.getClosePrice() * 100;
                        stockPrice.setChangeAmount(changeAmount);
                        stockPrice.setChangeRate(changeRate);
                    }
                    
                    stockPrice.setTradingAmount(stockPrice.getClosePrice() * stockPrice.getVolume());
                    stockPrices.add(stockPrice);
                }
            }
        }
        return stockPrices;
    }

    @Transactional(readOnly = true)
    public List<StockPrice> getStockPrices(Stock stock, LocalDate startDate, LocalDate endDate) {
        return stockPriceRepository.findByStockAndDateBetweenOrderByDateAsc(stock, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public StockPrice getLatestStockPrice(String stockCode) {
        return stockPriceRepository.findLatestByStockCode(stockCode)
                .orElseThrow(() -> new EntityNotFoundException("No stock price found for stock code: " + stockCode));
    }

    @Transactional(readOnly = true)
    public Page<StockPrice> getStockPriceHistory(String stockCode, Pageable pageable) {
        Stock stock = stockRepository.findByCode(stockCode)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found with code: " + stockCode));
        return stockPriceRepository.findByStockOrderByDateDesc(stock, pageable);
    }

    @Transactional(readOnly = true)
    public List<StockPrice> getWeeklyStockPrices(Long stockId, LocalDate startDate, LocalDate endDate) {
        return stockPriceRepository.findWeeklyPrices(stockId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<StockPrice> getMonthlyStockPrices(Long stockId, LocalDate startDate, LocalDate endDate) {
        return stockPriceRepository.findMonthlyPrices(stockId, startDate, endDate);
    }

    @Transactional
    public void deleteOldStockPrices(LocalDate cutoffDate) {
        stockPriceRepository.deleteByDateBefore(cutoffDate);
    }
}