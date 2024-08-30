package stock.stock_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.annotation.Backoff;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.core.KafkaTemplate;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;
import stock.stock_service.repository.StockPriceRepository;
import stock.stock_service.repository.StockRepository;
import stock.stock_service.exception.InvalidDataException;
import stock.stock_service.util.ApiRateLimiter;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.support.SendResult;
import org.springframework.retry.RetryContext;

@Service
public class StockPriceService {

    private static final int BATCH_SIZE = 1000;

    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);
    private static final String NAVER_STOCK_API_URL = "https://fchart.stock.naver.com/sise.nhn?symbol={symbol}&timeframe=day&count={count}&requestType=0";

    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiRateLimiter apiRateLimiter;

    @Autowired
    private KafkaTemplate<String, StockPrice> kafkaTemplate;

    private static final String DEAD_LETTER_TOPIC = "dead-letter-stock-prices";

    @Autowired
    private RetryTemplate retryTemplate;

    @Transactional
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void fetchAndSaveStockPrices(Stock stock, int days) {
        log.info("Fetching stock data for {} with days {}", stock.getCode(), days);
        apiRateLimiter.checkRateLimit();
        String apiResponse = restTemplate.getForObject(NAVER_STOCK_API_URL, String.class, stock.getCode(), days);
        List<StockPrice> stockPrices = parseApiResponse(apiResponse, stock);
        
        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> saveStockPricesInBatches(stockPrices));
        CompletableFuture<Void> kafkaFuture = CompletableFuture.runAsync(() -> sendStockPricesToKafka(stockPrices));
        
        CompletableFuture.allOf(saveFuture, kafkaFuture).join();
        
        log.info("Processed {} price records for stock {}", stockPrices.size(), stock.getCode());
    }

    private void saveStockPricesInBatches(List<StockPrice> stockPrices) {
        for (int i = 0; i < stockPrices.size(); i += BATCH_SIZE) {
            List<StockPrice> batch = stockPrices.subList(i, Math.min(i + BATCH_SIZE, stockPrices.size()));
            try {
                stockPriceRepository.saveAll(batch);
                log.debug("Saved batch of {} stock prices", batch.size());
            } catch (Exception e) {
                log.error("Error saving batch of stock prices", e);
                // 여기서 실패한 배치를 재시도하거나 dead letter queue로 보낼 수 있습니다.
            }
        }
    }

    @Transactional
    public void saveStockPrices(List<StockPrice> stockPrices) {
        stockPriceRepository.saveAll(stockPrices);
    }

    private void sendStockPricesToKafka(List<StockPrice> stockPrices) {
        stockPrices.forEach(this::sendStockPriceToKafkaWithRetry);
    }

    private void sendStockPriceToKafkaWithRetry(StockPrice stockPrice) {
        try {
            retryTemplate.execute(new RetryCallback<Void, Exception>() {
                @Override
                public Void doWithRetry(RetryContext context) throws Exception {
                    CompletableFuture<SendResult<String, StockPrice>> future = 
                        kafkaTemplate.send("stock-prices", stockPrice.getStock().getCode(), stockPrice);
                    
                    future.thenAccept(result -> log.debug("Sent stock price to Kafka: {}", stockPrice))
                          .exceptionally(ex -> {
                              log.error("Error sending stock price to Kafka", ex);
                              throw new RuntimeException("Failed to send message to Kafka", ex);
                          });
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("Failed to send stock price to Kafka after retries", e);
            sendToDeadLetterQueue(stockPrice);
        }
    }
    
    private void sendToDeadLetterQueue(StockPrice stockPrice) {
        try {
            kafkaTemplate.send(DEAD_LETTER_TOPIC, stockPrice.getStock().getCode(), stockPrice)
                .thenAccept(result -> log.info("Sent stock price to Dead Letter Queue: {}", stockPrice))
                .exceptionally(ex -> {
                    log.error("Error sending stock price to Dead Letter Queue", ex);
                    return null;
                });
        } catch (Exception e) {
            log.error("Failed to send stock price to Dead Letter Queue", e);
        }
    }

    @Cacheable(value = "stock-prices", key = "#stock.code + ':' + #count")
    public List<StockPrice> fetchAndParseStockPrices(Stock stock, int count) {
        log.info("Fetching and parsing stock data for {} with count {}", stock.getCode(), count);
        String apiResponse = restTemplate.getForObject(NAVER_STOCK_API_URL, String.class, stock.getCode(), count);
        return parseApiResponse(apiResponse, stock);
    }

    @CacheEvict(value = "stock-prices", key = "#stock.code + ':' + #count")
    public void evictStockPricesCache(Stock stock, int count) {
        log.info("Evicting cache for stock {} with count {}", stock.getCode(), count);
    }

    private List<StockPrice> parseApiResponse(String apiResponse, Stock stock) {
        List<StockPrice> stockPrices = new ArrayList<>();
        String[] lines = apiResponse.split("\n");
        for (String line : lines) {
            if (line.startsWith("<item data=")) {
                String[] data = line.split("\\|");
                if (data.length == 6) {
                    StockPrice stockPrice = createStockPrice(stock, data);
                    validateStockPrice(stockPrice);
                    stockPrices.add(stockPrice);
                }
            }
        }
        return stockPrices;
    }

    private StockPrice createStockPrice(Stock stock, String[] data) {
        StockPrice stockPrice = new StockPrice();
        stockPrice.setStock(stock);
        stockPrice.setDate(LocalDate.parse(data[0].substring(11), DateTimeFormatter.BASIC_ISO_DATE));
        stockPrice.setOpenPrice(Integer.parseInt(data[1]));
        stockPrice.setHighPrice(Integer.parseInt(data[2]));
        stockPrice.setLowPrice(Integer.parseInt(data[3]));
        stockPrice.setClosePrice(Integer.parseInt(data[4]));
        stockPrice.setVolume(Long.parseLong(data[5].replace("\"/>", "")));
        
        StockPrice previousPrice = stockPriceRepository.findFirstByStockOrderByDateDesc(stock).orElse(null);
        if (previousPrice != null) {
            int changeAmount = stockPrice.getClosePrice() - previousPrice.getClosePrice();
            double changeRate = (double) changeAmount / previousPrice.getClosePrice() * 100;
            stockPrice.setChangeAmount(changeAmount);
            stockPrice.setChangeRate(changeRate);
        }
        
        stockPrice.setTradingAmount(stockPrice.getClosePrice() * stockPrice.getVolume());
        return stockPrice;
    }

    private void validateStockPrice(StockPrice stockPrice) {
        if (stockPrice.getOpenPrice() <= 0 || stockPrice.getClosePrice() <= 0) {
            throw new InvalidDataException("Invalid price data for stock: " + stockPrice.getStock().getCode());
        }
    }

    public List<StockPrice> fetchMultipleStocks(List<Stock> stocks, int count) {
        List<CompletableFuture<List<StockPrice>>> futures = stocks.stream()
            .map(stock -> CompletableFuture.supplyAsync(() -> fetchAndParseStockPrices(stock, count)))
            .collect(Collectors.toList());

        return futures.stream()
            .flatMap(future -> future.join().stream())
            .collect(Collectors.toList());
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