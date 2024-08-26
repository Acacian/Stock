package stock.stock_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import stock.stock_service.model.Stock;
import stock.stock_service.model.StockPrice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, StockPrice.StockPriceId> {

    @Cacheable(value = "stockPrices", key = "#stockCode + '-' + #date")
    @Query("SELECT sp FROM StockPrice sp WHERE sp.stock.code = :stockCode AND sp.date = :date")
    Optional<StockPrice> findByStockCodeAndDate(String stockCode, LocalDate date);

    @Modifying
    @Transactional
    @CacheEvict(value = "stockPrices", key = "#stockPrice.stock.code + '-' + #stockPrice.date")
    @Query("UPDATE StockPrice sp SET sp.openPrice = :#{#stockPrice.openPrice}, " +
           "sp.highPrice = :#{#stockPrice.highPrice}, sp.lowPrice = :#{#stockPrice.lowPrice}, " +
           "sp.closePrice = :#{#stockPrice.closePrice}, sp.volume = :#{#stockPrice.volume} " +
           "WHERE sp.stock.code = :#{#stockPrice.stock.code} AND sp.date = :#{#stockPrice.date}")
    void updateStockPrice(StockPrice stockPrice);

    @Cacheable(value = "latestStockPrices", key = "#stock.id")
    Optional<StockPrice> findFirstByStockOrderByDateDesc(Stock stock);

    @Cacheable(value = "latestStockPrices", key = "#stockCode")
    @Query("SELECT sp FROM StockPrice sp WHERE sp.stock.code = :stockCode ORDER BY sp.date DESC LIMIT 1")
    Optional<StockPrice> findLatestByStockCode(@Param("stockCode") String stockCode);

    @Cacheable(value = "stockPriceRanges", key = "#stock.id + '-' + #startDate + '-' + #endDate")
    List<StockPrice> findByStockAndDateBetweenOrderByDateAsc(Stock stock, LocalDate startDate, LocalDate endDate);

    Page<StockPrice> findByStockOrderByDateDesc(Stock stock, Pageable pageable);

    @Cacheable(value = "weeklyPrices", key = "#stockId + '-' + #startDate + '-' + #endDate")
    @Query("SELECT new StockPrice(MAX(sp.id), sp.stock, sp.date, AVG(sp.openPrice), MAX(sp.highPrice), " +
           "MIN(sp.lowPrice), sp.closePrice, SUM(sp.volume), SUM(sp.tradingAmount)) " +
           "FROM StockPrice sp WHERE sp.stock.id = :stockId AND sp.date BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEARWEEK', sp.date) ORDER BY sp.date")
    List<StockPrice> findWeeklyPrices(@Param("stockId") Long stockId, 
                                      @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);

    @Cacheable(value = "monthlyPrices", key = "#stockId + '-' + #startDate + '-' + #endDate")
    @Query("SELECT new StockPrice(MAX(sp.id), sp.stock, sp.date, AVG(sp.openPrice), MAX(sp.highPrice), " +
           "MIN(sp.lowPrice), sp.closePrice, SUM(sp.volume), SUM(sp.tradingAmount)) " +
           "FROM StockPrice sp WHERE sp.stock.id = :stockId AND sp.date BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', sp.date), FUNCTION('MONTH', sp.date) ORDER BY sp.date")
    List<StockPrice> findMonthlyPrices(@Param("stockId") Long stockId, 
                                       @Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);

    @Modifying
    @Transactional
    @CacheEvict(value = {"stockPrices", "latestStockPrices", "stockPriceRanges", "weeklyPrices", "monthlyPrices"}, allEntries = true)
    @Query("DELETE FROM StockPrice sp WHERE sp.date < :cutoffDate")
    void deleteByDateBefore(@Param("cutoffDate") LocalDate cutoffDate);
}