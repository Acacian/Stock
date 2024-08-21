package stock.stock_service.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;
import stock.stock_service.model.StockPrice;
import stock.stock_service.kafka.StockEvent;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Bean
    public KStream<String, StockPrice> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, StockPrice> stream = streamsBuilder.stream("stock-prices", 
            Consumed.with(Serdes.String(), new JsonSerde<>(StockPrice.class)));

        stream
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
            .aggregate(
                () -> new StockPriceAggregate(),
                (key, value, aggregate) -> aggregate.update(value),
                Materialized.with(Serdes.String(), new JsonSerde<>(StockPriceAggregate.class))
            )
            .toStream()
            .filter((key, value) -> value.isSignificantChange())
            .map((key, value) -> KeyValue.pair(key.key(), new StockEvent(key.key(), value.getCurrentPrice(), value.getChangePercentage())))
            .to("stock-events", Produced.with(Serdes.String(), new JsonSerde<>(StockEvent.class)));

        return stream;
    }

    private static class StockPriceAggregate {
        private double initialPrice;
        private double currentPrice;
        private int count;

        @SuppressWarnings("unused")
        public StockPriceAggregate update(StockPrice price) {
            if (count == 0) {
                initialPrice = price.getClosePrice();
            }
            currentPrice = price.getClosePrice();
            count++;
            return this;
        }

        @SuppressWarnings("unused")
        public boolean isSignificantChange() {
            if (count < 2) return false;
            double changePercentage = (currentPrice - initialPrice) / initialPrice * 100;
            return Math.abs(changePercentage) >= 5; // 5% 이상 변동 시 유의미한 변화로 간주
        }

        @SuppressWarnings("unused")
        public double getCurrentPrice() {
            return currentPrice;
        }

        @SuppressWarnings("unused")
        public double getChangePercentage() {
            return (currentPrice - initialPrice) / initialPrice * 100;
        }
    }
}