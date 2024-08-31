package stock.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        headers.add("X-XSS-Protection", "1; mode=block");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("Referrer-Policy", "no-referrer-when-downgrade");
        headers.add("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none';");

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}