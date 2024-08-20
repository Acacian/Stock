package stock.stock_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stock.stock_service.model.Stock;
import stock.stock_service.repository.StockRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Stock getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Stock getStockByCode(String code) {
        return stockRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found with code: " + code));
    }

    @Transactional
    public Stock createStock(Stock stock) {
        return stockRepository.save(stock);
    }

    @Transactional
    public Stock updateStock(Long id, Stock stockDetails) {
        Stock stock = getStockById(id);
        stock.setName(stockDetails.getName());
        stock.setCode(stockDetails.getCode());
        stock.setMarketType(stockDetails.getMarketType());
        stock.setSector(stockDetails.getSector());
        stock.setMarketCap(stockDetails.getMarketCap());
        return stockRepository.save(stock);
    }

    @Transactional
    public void deleteStock(Long id) {
        Stock stock = getStockById(id);
        stockRepository.delete(stock);
    }

    @Transactional(readOnly = true)
    public Page<Stock> searchStocks(String query, Pageable pageable) {
        return stockRepository.findByNameContainingOrCodeContaining(query, query, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Stock> getStocksByMarketType(Stock.MarketType marketType, Pageable pageable) {
        return stockRepository.findByMarketType(marketType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Stock> searchStocks(String name, String code, Stock.MarketType marketType, 
                                    String sector, Long minMarketCap, Long maxMarketCap, 
                                    Pageable pageable) {
        return stockRepository.searchStocks(name, code, marketType, sector, minMarketCap, maxMarketCap, pageable);
    }

    @Transactional(readOnly = true)
    public List<Stock> getStocksToUpdate() {
        // KOSPI와 KOSDAQ의 상위 100개 종목을 반환
        List<Stock> kospiStocks = stockRepository.findTop100ByMarketTypeOrderByMarketCapDesc(Stock.MarketType.KOSPI);
        List<Stock> kosdaqStocks = stockRepository.findTop100ByMarketTypeOrderByMarketCapDesc(Stock.MarketType.KOSDAQ);
        
        kospiStocks.addAll(kosdaqStocks);
        return kospiStocks;
    }
}