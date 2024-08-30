package stock.stock_service.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StockPriceId implements Serializable {

    private Long stock;
    private LocalDate date;

    // 기본 생성자
    public StockPriceId() {}

    public StockPriceId(Long stock, LocalDate date) {
        this.stock = stock;
        this.date = date;
    }

    // Getters and Setters
    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // equals 메서드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockPriceId that = (StockPriceId) o;
        return Objects.equals(stock, that.stock) && Objects.equals(date, that.date);
    }

    // hashCode 메서드
    @Override
    public int hashCode() {
        return Objects.hash(stock, date);
    }
}
