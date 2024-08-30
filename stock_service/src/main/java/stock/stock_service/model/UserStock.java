package stock.stock_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.annotation.Cacheable;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stocks")
@Getter
@Setter
@Cacheable("userStocks")
@IdClass(UserStockId.class)  // 복합 키를 정의
public class UserStock implements Serializable {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private boolean watchlist;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "average_price", nullable = false)
    private int averagePrice;

    @Column(name = "last_trade_date")
    private LocalDateTime lastTradeDate;
}
