package stock.api_gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
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
                .filters(f -> f.filter(rateLimitingFilter))
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
}