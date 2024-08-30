package stock.stock_service.service;

import org.springframework.stereotype.Service;
import stock.stock_service.model.StockPrice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicalIndicatorServiceImpl implements TechnicalIndicatorService {

    @Override
    public List<Double> calculateMovingAverage(List<StockPrice> prices, int period) {
        List<Double> closePrices = prices.stream()
                .map(StockPrice::getClosePrice)
                .map(Double::valueOf)
                .collect(Collectors.toList());

        List<Double> movingAverages = new ArrayList<>();
        for (int i = period - 1; i < closePrices.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += closePrices.get(j);
            }
            movingAverages.add(sum / period);
        }
        return movingAverages;
    }

    @Override
    public List<Double> calculateBollingerBands(List<StockPrice> prices, int period, double k) {
        List<Double> ma = calculateMovingAverage(prices, period);
        List<Double> upperBand = new ArrayList<>();
        List<Double> lowerBand = new ArrayList<>();

        List<Double> closePrices = prices.stream()
                .map(StockPrice::getClosePrice)
                .map(Double::valueOf)
                .collect(Collectors.toList());

        for (int i = period - 1; i < closePrices.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += Math.pow(closePrices.get(j) - ma.get(i - period + 1), 2);
            }
            double stdDev = Math.sqrt(sum / period);
            upperBand.add(ma.get(i - period + 1) + k * stdDev);
            lowerBand.add(ma.get(i - period + 1) - k * stdDev);
        }

        List<Double> result = new ArrayList<>();
        result.addAll(upperBand);
        result.addAll(ma);
        result.addAll(lowerBand);
        return result;
    }

    @Override
    public List<Double> calculateMACD(List<StockPrice> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        List<Double> closePrices = prices.stream()
                .map(StockPrice::getClosePrice)
                .map(Double::valueOf)
                .collect(Collectors.toList());

        List<Double> shortEMA = calculateEMA(closePrices, shortPeriod);
        List<Double> longEMA = calculateEMA(closePrices, longPeriod);

        List<Double> macd = new ArrayList<>();
        for (int i = 0; i < shortEMA.size(); i++) {
            macd.add(shortEMA.get(i) - longEMA.get(i));
        }

        List<Double> signal = calculateEMA(macd, signalPeriod);
        List<Double> histogram = new ArrayList<>();
        for (int i = 0; i < signal.size(); i++) {
            histogram.add(macd.get(i + macd.size() - signal.size()) - signal.get(i));
        }

        List<Double> result = new ArrayList<>();
        result.addAll(macd);
        result.addAll(signal);
        result.addAll(histogram);
        return result;
    }

    private List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        ema.add(prices.get(0));
        for (int i = 1; i < prices.size(); i++) {
            double newEma = (prices.get(i) - ema.get(i - 1)) * multiplier + ema.get(i - 1);
            ema.add(newEma);
        }
        return ema;
    }

    @Override
    public List<Double> calculateRSI(List<StockPrice> prices, int period) {
        List<Double> rsiValues = new ArrayList<>();
        if (prices.size() <= period) {
            return rsiValues;
        }

        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            double difference = prices.get(i).getClosePrice() - prices.get(i - 1).getClosePrice();
            gains.add(Math.max(difference, 0));
            losses.add(Math.max(-difference, 0));
        }

        double avgGain = gains.subList(0, period).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgLoss = losses.subList(0, period).stream().mapToDouble(Double::doubleValue).average().orElse(0);

        for (int i = period; i < prices.size(); i++) {
            double rs = avgGain / avgLoss;
            double rsi = 100 - (100 / (1 + rs));
            rsiValues.add(rsi);

            avgGain = (avgGain * (period - 1) + gains.get(i - 1)) / period;
            avgLoss = (avgLoss * (period - 1) + losses.get(i - 1)) / period;
        }

        return rsiValues;
    }
}