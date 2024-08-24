package stock.api_gateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import jakarta.annotation.PostConstruct;

public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private RateLimiter rateLimiter;

    @Value("${resilience4j.ratelimiter.instances.default.limitForPeriod:100}")
    private int limitForPeriod;

    @Value("${resilience4j.ratelimiter.instances.default.limitRefreshPeriod:PT1S}")
    private String limitRefreshPeriod;

    @Value("${resilience4j.ratelimiter.instances.default.timeoutDuration:PT5S}")
    private String timeoutDuration;

    public RateLimitingFilter() {
        super(Config.class);
    }

    @PostConstruct
    public void init() {
        System.out.println("limitForPeriod: " + limitForPeriod);
        System.out.println("limitRefreshPeriod: " + limitRefreshPeriod);
        System.out.println("timeoutDuration: " + timeoutDuration);

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(Duration.parse(limitRefreshPeriod))
                .timeoutDuration(Duration.parse(timeoutDuration))
                .build();
        this.rateLimiter = RateLimiter.of("api-rate-limiter", config);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            boolean permitAcquired = rateLimiter.acquirePermission();
            if (!permitAcquired) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuration properties can be added here if needed in the future
    }
}