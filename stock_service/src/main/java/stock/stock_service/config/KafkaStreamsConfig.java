package stock.stock_service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;
import stock.stock_service.model.StockPrice;
import stock.stock_service.service.TechnicalIndicatorService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    private final TechnicalIndicatorService technicalIndicatorService;
    private final JsonSerde<List<Double>> listDoubleSerde = new JsonSerde<>(new TypeReference<List<Double>>() {});

    public KafkaStreamsConfig(TechnicalIndicatorService technicalIndicatorService) {
        this.technicalIndicatorService = technicalIndicatorService;
    }

    @Bean
    public KStream<String, StockPrice> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, StockPrice> stream = streamsBuilder.stream("stock-prices", 
            Consumed.with(Serdes.String(), new JsonSerde<>(StockPrice.class)));

        calculateMovingAverages(stream);
        calculateBollingerBands(stream);
        calculateMACD(stream);
        calculateRSI(stream);

        return stream;
    }

    private void calculateMovingAverages(KStream<String, StockPrice> stream) {
        stream.groupByKey()
              .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(26)))
              .aggregate(
                  ArrayList<StockPrice>::new,
                  (key, value, aggregate) -> {
                      aggregate.add(value);
                      return aggregate;
                  },
                  Materialized.<String, List<StockPrice>, WindowStore<Bytes, byte[]>>as("moving-average-store")
                      .withValueSerde(new JsonSerde<>(new TypeReference<List<StockPrice>>() {}))
              )
              .toStream()
              .map((key, prices) -> {
                  List<Double> ma12 = technicalIndicatorService.calculateMovingAverage(prices, 12);
                  List<Double> ma20 = technicalIndicatorService.calculateMovingAverage(prices, 20);
                  List<Double> ma26 = technicalIndicatorService.calculateMovingAverage(prices, 26);
                  List<Double> result = new ArrayList<>();
                  result.add(ma12.get(ma12.size() - 1));
                  result.add(ma20.get(ma20.size() - 1));
                  result.add(ma26.get(ma26.size() - 1));
                  return KeyValue.pair(key.key(), result);
              })
              .to("moving-averages", Produced.with(Serdes.String(), listDoubleSerde));
    }

    private void calculateBollingerBands(KStream<String, StockPrice> stream) {
        stream.groupByKey()
              .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(20)))
              .aggregate(
                  ArrayList<StockPrice>::new,
                  (key, value, aggregate) -> {
                      aggregate.add(value);
                      return aggregate;
                  },
                  Materialized.<String, List<StockPrice>, WindowStore<Bytes, byte[]>>as("bollinger-bands-store")
                      .withValueSerde(new JsonSerde<>(new TypeReference<List<StockPrice>>() {}))
              )
              .toStream()
              .map((key, prices) -> {
                  List<Double> bollingerBands = technicalIndicatorService.calculateBollingerBands(prices, 20, 2.0);
                  return KeyValue.pair(key.key(), bollingerBands);
              })
              .to("bollinger-bands", Produced.with(Serdes.String(), listDoubleSerde));
    }

    private void calculateMACD(KStream<String, StockPrice> stream) {
        stream.groupByKey()
              .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(26)))
              .aggregate(
                  ArrayList<StockPrice>::new,
                  (key, value, aggregate) -> {
                      aggregate.add(value);
                      return aggregate;
                  },
                  Materialized.<String, List<StockPrice>, WindowStore<Bytes, byte[]>>as("macd-store")
                      .withValueSerde(new JsonSerde<>(new TypeReference<List<StockPrice>>() {}))
              )
              .toStream()
              .map((key, prices) -> {
                  List<Double> macd = technicalIndicatorService.calculateMACD(prices, 12, 26, 9);
                  return KeyValue.pair(key.key(), macd);
              })
              .to("macd", Produced.with(Serdes.String(), listDoubleSerde));
    }

    private void calculateRSI(KStream<String, StockPrice> stream) {
        stream.groupByKey()
              .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(14)))
              .aggregate(
                  ArrayList<StockPrice>::new,
                  (key, value, aggregate) -> {
                      aggregate.add(value);
                      return aggregate;
                  },
                  Materialized.<String, List<StockPrice>, WindowStore<Bytes, byte[]>>as("rsi-store")
                      .withValueSerde(new JsonSerde<>(new TypeReference<List<StockPrice>>() {}))
              )
              .toStream()
              .map((key, prices) -> {
                  List<Double> rsiValues = technicalIndicatorService.calculateRSI(prices, 14);
                  List<Double> result = rsiValues.isEmpty() ? new ArrayList<>() : List.of(rsiValues.get(rsiValues.size() - 1));
                  return KeyValue.pair(key.key(), result);
              })
              .to("rsi", Produced.with(Serdes.String(), listDoubleSerde));
    }
}