package stock.stock_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;
import stock.stock_service.repository.StockPriceRepository;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockPriceService {

    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, StockPrice> kafkaTemplate;

    private static final String NAVER_STOCK_API_URL = "https://fchart.stock.naver.com/sise.nhn?symbol={symbol}&timeframe=day&count={count}&requestType=0";

    public void fetchAndSaveStockPrices(Stock stock, int count) {
        String apiResponse = restTemplate.getForObject(NAVER_STOCK_API_URL, String.class, stock.getCode(), count);
        List<StockPrice> stockPrices = parseApiResponse(apiResponse, stock);
        for (StockPrice stockPrice : stockPrices) {
            kafkaTemplate.send("stock-prices", stockPrice);
        }
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
                    stockPrices.add(stockPrice);
                }
            }
        }
        return stockPrices;
    }

    public List<StockPrice> getStockPrices(Stock stock, LocalDate startDate, LocalDate endDate) {
        return stockPriceRepository.findByStockAndDateBetweenOrderByDateAsc(stock, startDate, endDate);
    }
}
