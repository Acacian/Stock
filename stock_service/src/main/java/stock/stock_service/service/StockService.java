package stock.stock_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import stock.stock_service.model.Stock;
import stock.stock_service.repository.StockRepository;

import java.util.List;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }

    public Page<Stock> searchStocks(String query, Pageable pageable) {
        return stockRepository.findByNameContainingOrCodeContaining(query, query, pageable);
    }

    public Page<Stock> getStocksByMarketType(Stock.MarketType marketType, Pageable pageable) {
        return stockRepository.findByMarketType(marketType, pageable);
    }

    public Stock getStockById(Long id) {
        return stockRepository.findById(id).orElseThrow(() -> new RuntimeException("Stock not found"));
    }

    public Stock createStock(Stock stock) {
        return stockRepository.save(stock);
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }
}