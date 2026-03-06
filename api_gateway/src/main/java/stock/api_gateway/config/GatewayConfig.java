package stock.api_gateway.config;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import reactor.core.publisher.Mono;
import stock.api_gateway.filter.JwtAuthenticationFilter;
import stock.api_gateway.filter.RateLimitingFilter;

@Configuration
public class GatewayConfig {

    @Value("${app.routes.newsfeed-uri:lb://NEWSFEED-SERVICE}")
    private String newsfeedServiceUri;

    @Value("${app.routes.social-uri:lb://SOCIAL-SERVICE}")
    private String socialServiceUri;

    @Value("${app.routes.user-uri:lb://USER-SERVICE}")
    private String userServiceUri;

    @Value("${app.routes.stock-uri:lb://STOCK-SERVICE}")
    private String stockServiceUri;

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
                .uri(newsfeedServiceUri))
            .route("social_service", r -> r.path("/api/social/**")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri(socialServiceUri))
            .route("user_service_public", r -> r.path("/api/auth/register", "/api/auth/login", "/api/auth/verify")
            .filters(f -> f
                .filter((exchange, chain) -> {
                    System.out.println("Request received at user_service_public route: " + exchange.getRequest().getPath());
                    return chain.filter(exchange);
                })
                .filter(rateLimitingFilter))
                .uri(userServiceUri))
            .route("user_service_check", r -> r.path("/api/auth/check")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri(userServiceUri))
            .route("user_service_protected", r -> r.path("/api/auth/**")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri(userServiceUri))
            .route("stock_service", r -> r.path("/api/stocks/**")
                .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                               .filter(rateLimitingFilter))
                .uri(stockServiceUri))
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
            if (ex instanceof Exception) {
                if (ex.getMessage().contains("Authentication failed")) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    errorMessage = "Authentication failed";
                } else if (ex.getMessage().contains("Rate limit exceeded")) {
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    errorMessage = "Rate limit exceeded";
                }
            }

            byte[] bytes = ("{\"error\":\"" + errorMessage + "\"}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        };
    }
}
