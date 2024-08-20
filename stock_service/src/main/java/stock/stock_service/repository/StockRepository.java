package stock.stock_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stock.stock_service.model.Stock;

import java.util.Optional;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByCode(String code);

    List<Stock> findByMarketType(Stock.MarketType marketType);

    @Query("SELECT s FROM Stock s WHERE " +
           "(:name IS NULL OR s.name LIKE %:name%) AND " +
           "(:code IS NULL OR s.code LIKE %:code%) AND " +
           "(:marketType IS NULL OR s.marketType = :marketType) AND " +
           "(:sector IS NULL OR s.sector = :sector) AND " +
           "(:minMarketCap IS NULL OR s.marketCap >= :minMarketCap) AND " +
           "(:maxMarketCap IS NULL OR s.marketCap <= :maxMarketCap)")
    List<Stock> searchStocks(
            @Param("name") String name,
            @Param("code") String code,
            @Param("marketType") Stock.MarketType marketType,
            @Param("sector") String sector,
            @Param("minMarketCap") Long minMarketCap,
            @Param("maxMarketCap") Long maxMarketCap
    );

    List<Stock> findTop100ByMarketTypeOrderByMarketCapDesc(Stock.MarketType marketType);
}