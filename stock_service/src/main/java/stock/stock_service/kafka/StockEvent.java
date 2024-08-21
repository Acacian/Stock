package stock.stock_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockEvent {
    private String stockCode;
    private double price;
    private double changePercentage;
}