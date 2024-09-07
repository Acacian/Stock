package stock.api_gateway.config;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import reactor.core.publisher.Mono;
import stock.api_gateway.filter.JwtAuthenticationFilter;
import stock.api_gateway.filter.RateLimitingFilter;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("newsfeed_service", r -> r.path("/api/newsfeed/**")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri("http://newsfeed-service:8083"))
            .route("social_service", r -> r.path("/api/social/**")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri("http://social-service:8084"))
            .route("user_service_public", r -> r.path("/api/auth/register", "/api/auth/login", "/api/auth/verify")
            .filters(f -> f
                .filter((exchange, chain) -> {
                    System.out.println("Request received at user_service_public route: " + exchange.getRequest().getPath());
                    return chain.filter(exchange);
                })
                .filter(rateLimitingFilter))
                .uri("http://user-service:8086"))
            .route("user_service_check", r -> r.path("/api/auth/check")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri("http://user-service:8086"))
            .route("user_service_protected", r -> r.path("/api/auth/**")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri("http://user-service:8086"))
            .route("stock_service", r -> r.path("/api/stocks/**")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri("http://stock-service:8085"))
            .build();
    }

    @Bean
    public WebExceptionHandler errorWebExceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {

            System.out.println("Exception occurred: " + ex.getMessage());
            ex.printStackTrace();
            
            if (exchange.getResponse().isCommitted()) {
                return Mono.error(ex);
            }

            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String errorMessage = "An unexpected error occurred";
            if (ex instanceof AuthenticationException) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                errorMessage = "Authentication failed";
            } else if (ex instanceof Exception && ex.getMessage().contains("Rate limit exceeded")) {
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                errorMessage = "Rate limit exceeded";
            }

            byte[] bytes = ("{\"error\":\"" + errorMessage + "\"}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        };
    }
}