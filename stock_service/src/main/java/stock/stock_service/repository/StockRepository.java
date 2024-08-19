package stock.stock_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import stock.stock_service.model.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Page<Stock> findByNameContainingOrCodeContaining(String name, String code, Pageable pageable);
    Page<Stock> findByMarketType(Stock.MarketType marketType, Pageable pageable);
}
