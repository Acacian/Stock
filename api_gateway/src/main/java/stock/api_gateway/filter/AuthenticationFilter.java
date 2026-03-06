package stock.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpMethod;

import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private Key key;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            logger.info("Processing request: {}", request.getPath());

            if (request.getMethod() == HttpMethod.OPTIONS) {
                logger.info("Skipping OPTIONS request");
                return chain.filter(exchange);
            }

            if (isOpenEndpoint(request.getPath().toString())) {
                logger.info("Skipping authentication for open endpoint: {}", request.getPath());
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey("Authorization")) {
                logger.warn("No Authorization header found");
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header");
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            if (!validateToken(token)) {
                logger.warn("Invalid JWT token");
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }

            logger.info("Authentication successful for request: {}", request.getPath());
            return chain.filter(exchange);
        };
    }

    private boolean isOpenEndpoint(String path) {
        boolean isOpen = path.contains("/api/auth/register") || path.contains("/api/auth/login");
        logger.info("Checking if {} is an open endpoint: {}", path, isOpen);
        return isOpen;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        logger.error("Authentication error: {}", err);
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    private boolean validateToken(String token) {
        try {
            if (key == null) {
                key = new SecretKeySpec(hashSecret(jwtSecret), io.jsonwebtoken.SignatureAlgorithm.HS512.getJcaName());
            }
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            Date now = new Date();
            if (claims.getExpiration().before(now)) {
                logger.warn("Token has expired");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return false;
        }
    }

    private byte[] hashSecret(String secret) {
        try {
            return MessageDigest.getInstance("SHA-512")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize JWT signing key", e);
        }
    }

    public static class Config {
        // Configuration properties can be added here if needed in the future
    }
}
