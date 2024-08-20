package stock.stock_service.util;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;
import stock.stock_service.exception.RateLimitExceededException;

@Component
public class ApiRateLimiter {
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final int MAX_REQUESTS_PER_MINUTE = 60;

    public void checkRateLimit() throws RateLimitExceededException {
        if (requestCount.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            throw new RateLimitExceededException("API rate limit exceeded");
        }
    }

    public void resetRequestCount() {
        requestCount.set(0);
    }
}