package stock.newsfeed_service.kafka;

public class StockEvent {
    private String stockCode;
    private double price;
    private double changePercentage;

    // Constructors
    public StockEvent() {}

    public StockEvent(String stockCode, double price, double changePercentage) {
        this.stockCode = stockCode;
        this.price = price;
        this.changePercentage = changePercentage;
    }

    // Getters and Setters
    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }
}