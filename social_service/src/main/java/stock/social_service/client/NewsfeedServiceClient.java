package stock.social_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import stock.social_service.kafka.SocialEvent;

@FeignClient(name = "newsfeed-service")
public interface NewsfeedServiceClient {

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackPostCreated")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/post-created")
    void postCreated(@RequestBody SocialEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackCommentCreated")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/comment-created")
    void commentCreated(@RequestBody SocialEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackPostLiked")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/post-liked")
    void postLiked(@RequestBody SocialEvent event);

    default void fallbackPostCreated(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for post created: " + event.toString());
    }

    default void fallbackCommentCreated(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for comment created: " + event.toString());
    }

    default void fallbackPostLiked(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for post liked: " + event.toString());
    }
}