package stock.user_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import stock.user_service.kafka.AuthEvent;

@FeignClient(name = "newsfeed-service")
public interface NewsfeedServiceClient {

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackUserAuthenticated")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/user-authenticated")
    void userAuthenticated(@RequestBody AuthEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackProfileUpdated")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/profile-updated")
    void profileUpdated(@RequestBody AuthEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackPasswordUpdated")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/password-updated")
    void passwordUpdated(@RequestBody AuthEvent event);

    default void fallbackUserAuthenticated(AuthEvent event, Throwable throwable) {
        System.out.println("Fallback for user authenticated: " + event.toString());
    }

    default void fallbackProfileUpdated(AuthEvent event, Throwable throwable) {
        System.out.println("Fallback for profile updated: " + event.toString());
    }

    default void fallbackPasswordUpdated(AuthEvent event, Throwable throwable) {
        System.out.println("Fallback for password updated: " + event.toString());
    }
}