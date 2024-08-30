package stock.stock_service.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import stock.stock_service.model.Stock;
import stock.stock_service.service.StockPriceService;
import stock.stock_service.service.StockService;

@Configuration
public class StockBatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockPriceService stockPriceService;

    @Bean
    public Job updateStockPricesJob(Step updateStockPricesStep) {
        return new JobBuilder("updateStockPricesJob", jobRepository)
                .start(updateStockPricesStep)
                .build();
    }

    @Bean
    public Step updateStockPricesStep(ItemReader<Stock> reader, ItemProcessor<Stock, Stock> processor, ItemWriter<Stock> writer) {
        return new StepBuilder("updateStockPricesStep", jobRepository)
                .<Stock, Stock>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<Stock> stockReader() {
        return new ListItemReader<>(stockService.getAllStocksToUpdate());
    }

    @Bean
    public ItemProcessor<Stock, Stock> stockProcessor() {
        return stock -> {
            stockPriceService.fetchAndSaveStockPrices(stock, 1); // 1일치 데이터 업데이트
            return stock;
        };
    }

    @Bean
    public ItemWriter<Stock> stockWriter() {
        return stocks -> {
            // 필요한 경우 추가 처리
        };
    }
}