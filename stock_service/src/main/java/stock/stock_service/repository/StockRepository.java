package stock.stock_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import stock.stock_service.model.Stock;

import java.util.Optional;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Cacheable(value = "stocks", key = "#code")
    Optional<Stock> findByCode(String code);

    @Cacheable(value = "stocksByMarketType", key = "#marketType + '-' + #pageable")
    Page<Stock> findByMarketType(Stock.MarketType marketType, Pageable pageable);

    @Cacheable(value = "stockSearch", key = "#name + '-' + #code + '-' + #pageable")
    Page<Stock> findByNameContainingOrCodeContaining(String name, String code, Pageable pageable);

    @Transactional(readOnly = true)
    @Cacheable(value = "stockSearch", key = "#name + '-' + #code + '-' + #marketType + '-' + #sector + '-' + #minMarketCap + '-' + #maxMarketCap + '-' + #pageable")
    @Query("SELECT s FROM Stock s WHERE " +
           "(:name IS NULL OR s.name LIKE %:name%) AND " +
           "(:code IS NULL OR s.code LIKE %:code%) AND " +
           "(:marketType IS NULL OR s.marketType = :marketType) AND " +
           "(:sector IS NULL OR s.sector = :sector) AND " +
           "(:minMarketCap IS NULL OR s.marketCap >= :minMarketCap) AND " +
           "(:maxMarketCap IS NULL OR s.marketCap <= :maxMarketCap)")
    Page<Stock> searchStocks(
            @Param("name") String name,
            @Param("code") String code,
            @Param("marketType") Stock.MarketType marketType,
            @Param("sector") String sector,
            @Param("minMarketCap") Long minMarketCap,
            @Param("maxMarketCap") Long maxMarketCap,
            Pageable pageable
    );

    @Cacheable(value = "topStocks", key = "#marketType")
    List<Stock> findTop100ByMarketTypeOrderByMarketCapDesc(Stock.MarketType marketType);
}