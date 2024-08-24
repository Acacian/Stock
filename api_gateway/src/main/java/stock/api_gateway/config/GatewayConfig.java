package stock.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import stock.api_gateway.filter.RateLimitingFilter;

@Configuration
public class GatewayConfig {

    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter();
    }
}