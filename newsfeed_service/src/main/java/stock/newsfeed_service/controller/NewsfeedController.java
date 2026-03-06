package stock.newsfeed_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.newsfeed_service.dto.AuthEventDto;
import stock.newsfeed_service.dto.SocialEventDto;
import stock.newsfeed_service.model.NewsfeedItem;
import stock.newsfeed_service.service.NewsfeedService;

import java.util.List;

@RestController
@RequestMapping("/api/newsfeed")
public class NewsfeedController {

    @Autowired
    private NewsfeedService newsfeedService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<NewsfeedItem>> getNewsfeed(@PathVariable Long userId) {
        return ResponseEntity.ok(newsfeedService.getNewsfeed(userId));
    }

    @PostMapping("/user-followed")
    public ResponseEntity<Void> userFollowed(@RequestBody SocialEventDto event) {
        newsfeedService.addFollowActivity(event.getUserId(), event.getTargetId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/post-created")
    public ResponseEntity<Void> postCreated(@RequestBody SocialEventDto event) {
        newsfeedService.addPostActivity(event.getUserId(), event.getTargetId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment-created")
    public ResponseEntity<Void> commentCreated(@RequestBody SocialEventDto event) {
        newsfeedService.addCommentActivity(event.getUserId(), event.getTargetId(), event.getAdditionalId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/post-liked")
    public ResponseEntity<Void> postLiked(@RequestBody SocialEventDto event) {
        newsfeedService.addLikeActivity(event.getUserId(), event.getTargetId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/post-unliked")
    public ResponseEntity<Void> postUnliked(@RequestBody(required = false) SocialEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user-unfollowed")
    public ResponseEntity<Void> userUnfollowed(@RequestBody(required = false) SocialEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment-liked")
    public ResponseEntity<Void> commentLiked(@RequestBody(required = false) SocialEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment-unliked")
    public ResponseEntity<Void> commentUnliked(@RequestBody(required = false) SocialEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/follower-activity")
    public ResponseEntity<Void> followerActivity(@RequestBody(required = false) SocialEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user-authenticated")
    public ResponseEntity<Void> userAuthenticated(@RequestBody(required = false) AuthEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile-updated")
    public ResponseEntity<Void> profileUpdated(@RequestBody(required = false) AuthEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-updated")
    public ResponseEntity<Void> passwordUpdated(@RequestBody(required = false) AuthEventDto ignoredEvent) {
        return ResponseEntity.ok().build();
    }
}
