package stock.stock_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockEvent implements Serializable {
    private String stockCode;
    private double price;
    private double changePercentage;
}