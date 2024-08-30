package stock.stock_service.model;

import java.io.Serializable;
import java.util.Objects;

public class UserStockId implements Serializable {

    private Long userId;
    private Long stock;

    // 기본 생성자
    public UserStockId() {}

    public UserStockId(Long userId, Long stock) {
        this.userId = userId;
        this.stock = stock;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    // equals 메서드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStockId that = (UserStockId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(stock, that.stock);
    }

    // hashCode 메서드
    @Override
    public int hashCode() {
        return Objects.hash(userId, stock);
    }
}
