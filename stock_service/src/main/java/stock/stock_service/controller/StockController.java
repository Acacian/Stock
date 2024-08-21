package stock.stock_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;
import stock.stock_service.service.StockPriceService;
import stock.stock_service.service.StockService;

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
    public ResponseEntity<Page<Stock>> getAllStocks(Pageable pageable) {
        return ResponseEntity.ok(stockService.getAllStocks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getStockById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Stock> getStockByCode(@PathVariable String code) {
        return ResponseEntity.ok(stockService.getStockByCode(code));
    }

    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
        return ResponseEntity.ok(stockService.createStock(stock));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stock> updateStock(@PathVariable Long id, @RequestBody Stock stockDetails) {
        return ResponseEntity.ok(stockService.updateStock(id, stockDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Stock>> searchStocks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Stock.MarketType marketType,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) Long minMarketCap,
            @RequestParam(required = false) Long maxMarketCap,
            Pageable pageable) {
        return ResponseEntity.ok(stockService.searchStocks(name, code, marketType, sector, minMarketCap, maxMarketCap, pageable));
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<StockPrice>> getStockPrices(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Stock stock = stockService.getStockById(id);
        return ResponseEntity.ok(stockPriceService.getStockPrices(stock, startDate, endDate));
    }
    
    @GetMapping("/{id}/prices/weekly")
    public ResponseEntity<List<StockPrice>> getWeeklyStockPrices(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(stockPriceService.getWeeklyStockPrices(id, startDate, endDate));
    }

    @GetMapping("/{id}/prices/monthly")
    public ResponseEntity<List<StockPrice>> getMonthlyStockPrices(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(stockPriceService.getMonthlyStockPrices(id, startDate, endDate));
    }

    @GetMapping("/code/{code}/latest-price")
    public ResponseEntity<StockPrice> getLatestStockPrice(@PathVariable String code) {
        return ResponseEntity.ok(stockPriceService.getLatestStockPrice(code));
    }

    @GetMapping("/code/{code}/price-history")
    public ResponseEntity<Page<StockPrice>> getStockPriceHistory(
            @PathVariable String code,
            Pageable pageable) {
        return ResponseEntity.ok(stockPriceService.getStockPriceHistory(code, pageable));
    }
}