package stock.stock_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import stock.stock_service.model.Stock;

@FeignClient(name = "stock-service")
public interface StockServiceClient {

    @GetMapping("/api/stocks/{id}")
    Stock getStockById(@PathVariable Long id);

    @GetMapping("/api/stocks")
    Page<Stock> getAllStocks(Pageable pageable);

    @GetMapping("/api/stocks/search")
    Page<Stock> searchStocks(@RequestParam String query, Pageable pageable);

    @GetMapping("/api/stocks/market")
    Page<Stock> getStocksByMarketType(@RequestParam Stock.MarketType marketType, Pageable pageable);
}