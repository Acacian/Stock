package stock.stock_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;
import stock.stock_service.service.StockService;
import stock.stock_service.service.StockPriceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockPriceService stockPriceService;

    @GetMapping
    public Page<Stock> getAllStocks(@PageableDefault(size = 20) Pageable pageable) {
        return stockService.getAllStocks(pageable);
    }

    @GetMapping("/search")
    public Page<Stock> searchStocks(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return stockService.searchStocks(query, pageable);
    }

    @GetMapping("/market")
    public Page<Stock> getStocksByMarketType(
            @RequestParam Stock.MarketType marketType,
            @PageableDefault(size = 20) Pageable pageable) {
        return stockService.getStocksByMarketType(marketType, pageable);
    }

    @GetMapping("/{id}")
    public Stock getStockById(@PathVariable Long id) {
        return stockService.getStockById(id);
    }

    @PostMapping
    public Stock createStock(@RequestBody Stock stock) {
        return stockService.createStock(stock);
    }

    @GetMapping("/{id}/prices")
    public List<StockPrice> getStockPrices(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Stock stock = stockService.getStockById(id);
        return stockPriceService.getStockPrices(stock, startDate, endDate);
    }
}