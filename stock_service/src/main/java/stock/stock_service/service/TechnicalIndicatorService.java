package stock.stock_service.service;

import stock.stock_service.model.StockPrice;
import java.util.List;

public interface TechnicalIndicatorService {
    List<Double> calculateMovingAverage(List<StockPrice> prices, int period);
    List<Double> calculateBollingerBands(List<StockPrice> prices, int period, double k);
    List<Double> calculateMACD(List<StockPrice> prices, int shortPeriod, int longPeriod, int signalPeriod);
    List<Double> calculateRSI(List<StockPrice> prices, int period);
}