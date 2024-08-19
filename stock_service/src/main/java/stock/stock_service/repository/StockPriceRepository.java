package stock.stock_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;

import java.time.LocalDate;
import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    List<StockPrice> findByStockAndDateBetweenOrderByDateAsc(Stock stock, LocalDate startDate, LocalDate endDate);
}