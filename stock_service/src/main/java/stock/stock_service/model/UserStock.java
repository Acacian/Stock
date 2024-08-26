package stock.stock_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.annotation.Cacheable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stocks")
@Getter
@Setter
@Cacheable("userStocks")
public class UserStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private boolean watchlist;

    private int quantity;

    @Column(name = "average_price")
    private int averagePrice;

    @Column(name = "last_trade_date")
    private LocalDateTime lastTradeDate;
}