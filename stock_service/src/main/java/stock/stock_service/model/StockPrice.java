package stock.stock_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "stock_prices")
@Getter
@Setter
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "open_price", nullable = false)
    private int openPrice;
    
    @Column(name = "high_price", nullable = false)
    private int highPrice;
    
    @Column(name = "low_price", nullable = false)
    private int lowPrice;
    
    @Column(name = "close_price", nullable = false)
    private int closePrice;
    
    @Column(nullable = false)
    private long volume;
    
    @Column(name = "change_amount")
    private int changeAmount;
    
    @Column(name = "change_rate")
    private double changeRate;
    
    @Column(name = "trading_amount")
    private long tradingAmount;

    // Constructors
    public StockPrice() {}

    public StockPrice(Stock stock, LocalDate date, int openPrice, int highPrice, int lowPrice, int closePrice, long volume) {
        this.stock = stock;
        this.date = date;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    public StockPrice(Long id, Stock stock, LocalDate date, double openPrice, int highPrice, int lowPrice, int closePrice, long volume, long tradingAmount) {
        this.id = id;
        this.stock = stock;
        this.date = date;
        this.openPrice = (int) openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.tradingAmount = tradingAmount;
    }
}