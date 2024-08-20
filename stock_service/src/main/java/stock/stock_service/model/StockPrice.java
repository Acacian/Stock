package stock.stock_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_prices")
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(int openPrice) {
        this.openPrice = openPrice;
    }

    public int getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(int highPrice) {
        this.highPrice = highPrice;
    }

    public int getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(int lowPrice) {
        this.lowPrice = lowPrice;
    }

    public int getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(int closePrice) {
        this.closePrice = closePrice;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public int getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(int changeAmount) {
        this.changeAmount = changeAmount;
    }

    public double getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(double changeRate) {
        this.changeRate = changeRate;
    }

    public long getTradingAmount() {
        return tradingAmount;
    }

    public void setTradingAmount(long tradingAmount) {
        this.tradingAmount = tradingAmount;
    }
}