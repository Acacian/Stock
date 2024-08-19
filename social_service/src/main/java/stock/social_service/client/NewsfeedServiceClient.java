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

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackPostUnliked")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/post-unliked")
    void postUnliked(@RequestBody SocialEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackUserFollowed")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/user-followed")
    void userFollowed(@RequestBody SocialEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackUserUnfollowed")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/user-unfollowed")
    void userUnfollowed(@RequestBody SocialEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackCommentLiked")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/comment-liked")
    void commentLiked(@RequestBody SocialEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackCommentUnliked")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/comment-unliked")
    void commentUnliked(@RequestBody SocialEvent event);

    @CircuitBreaker(name = "newsfeedService", fallbackMethod = "fallbackFollowerActivity")
    @Retry(name = "newsfeedService")
    @PostMapping("/api/newsfeed/follower-activity")
    void followerActivity(@RequestBody SocialEvent event);

    default void fallbackPostCreated(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for post created: " + event.toString());
    }

    default void fallbackCommentCreated(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for comment created: " + event.toString());
    }

    default void fallbackPostLiked(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for post liked: " + event.toString());
    }

    default void fallbackPostUnliked(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for post unliked: " + event.toString());
    }

    default void fallbackUserFollowed(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for user followed: " + event.toString());
    }

    default void fallbackUserUnfollowed(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for user unfollowed: " + event.toString());
    }

    default void fallbackCommentLiked(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for comment liked: " + event.toString());
    }

    default void fallbackCommentUnliked(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for comment unliked: " + event.toString());
    }

    default void fallbackFollowerActivity(SocialEvent event, Throwable throwable) {
        System.out.println("Fallback for follower activity: " + event.toString());
    }
}