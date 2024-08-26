package stock.stock_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.annotation.Cacheable;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@Cacheable("stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "market_type", nullable = false)
    private MarketType marketType;
    
    private String sector;
    
    @Column(name = "market_cap")
    private Long marketCap;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum MarketType {
        KOSPI, KOSDAQ
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}