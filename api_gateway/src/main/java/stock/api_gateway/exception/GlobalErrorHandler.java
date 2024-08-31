package stock.api_gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorCode = "INTERNAL_SERVER_ERROR";
        if (ex instanceof IllegalArgumentException) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            errorCode = "BAD_REQUEST";
        }

        String errorMessage = String.format("{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}",
                errorCode, ex.getMessage(), exchange.getResponse().getStatusCode().value());
        
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}