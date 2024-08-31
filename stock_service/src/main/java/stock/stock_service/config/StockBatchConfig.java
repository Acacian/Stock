package stock.stock_service.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import stock.stock_service.model.Stock;
import stock.stock_service.service.StockPriceService;
import stock.stock_service.service.StockService;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableBatchProcessing
public class StockBatchConfig {

    private static final Logger log = LoggerFactory.getLogger(StockBatchConfig.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockPriceService stockPriceService;

    @Value("${stock.batch.chunk-size:100}")
    private int chunkSize;

    @Bean
    public Job updateStockPricesJob(Step updateStockPricesStep) {
        return new JobBuilder("updateStockPricesJob", jobRepository)
                .start(updateStockPricesStep)
                .build();
    }

    @Bean
    public Step updateStockPricesStep(ItemReader<Stock> reader, ItemProcessor<Stock, Stock> processor, ItemWriter<Stock> writer) {
        return new StepBuilder("updateStockPricesStep", jobRepository)
                .<Stock, Stock>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public ItemReader<Stock> stockReader() {
        return new ListItemReader<>(stockService.getAllStocksToUpdate());
    }

    @Bean
    public ItemProcessor<Stock, Stock> stockProcessor() {
        return stock -> {
            try {
                Long days = (Long) jobRepository.getLastJobExecution("updateStockPricesJob", new org.springframework.batch.core.JobParameters())
                        .getJobParameters().getParameters().get("days").getValue();
                stockPriceService.fetchAndSaveStockPrices(stock, days.intValue());
                stock.setLastUpdated(LocalDate.now());
                return stock;
            } catch (Exception e) {
                log.error("Error processing stock {}: {}", stock.getCode(), e.getMessage());
                return null;
            }
        };
    }

    @Bean
    public ItemWriter<Stock> stockWriter() {
        return new ItemWriter<Stock>() {
            @Override
            public void write(Chunk<? extends Stock> chunk) throws Exception {
                stockService.updateStocks(chunk.getItems());
            }
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("StockBatch-");
        executor.setConcurrencyLimit(10); // 동시 실행 스레드 수 제한
        return executor;
    }
}