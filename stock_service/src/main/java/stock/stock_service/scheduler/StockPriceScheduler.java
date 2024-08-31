package stock.stock_service.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@EnableScheduling
public class StockPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockPriceScheduler.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job updateStockPricesJob;

    @Value("${stock.scheduler.historical-days:1825}")
    private int historicalDays;

    @Scheduled(cron = "${stock.scheduler.daily.cron:0 0 18 * * MON-FRI}")
    public void updateDailyStockPrices() {
        runJob(1);
    }

    @Scheduled(cron = "${stock.scheduler.init.cron:0 0 1 * * ?}")
    public void initializeHistoricalData() {
        runJob(historicalDays);
    }

    private void runJob(int days) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addLong("days", (long) days)
                .toJobParameters();
        
        try {
            jobLauncher.run(updateStockPricesJob, jobParameters);
        } catch (Exception e) {
            log.error("Error running stock price update job: {}", e.getMessage());
        }
    }
}