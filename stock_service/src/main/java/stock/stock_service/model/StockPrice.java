package stock.stock_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int openPrice;

    @Column(nullable = false)
    private int highPrice;

    @Column(nullable = false)
    private int lowPrice;

    @Column(nullable = false)
    private int closePrice;

    @Column(nullable = false)
    private long volume;
}