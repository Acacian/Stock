package stock.newsfeed_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.newsfeed_service.service.NewsfeedService;
import stock.newsfeed_service.kafka.SocialEvent;
import stock.newsfeed_service.kafka.UserEvent;

import java.util.List;

@RestController
@RequestMapping("/api/newsfeed")
public class NewsfeedController {

    @Autowired
    private NewsfeedService newsfeedService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<String>> getNewsfeed(@PathVariable Long userId) {
        return ResponseEntity.ok(newsfeedService.getNewsfeed(userId));
    }

    @PostMapping("/post-created")
    public void postCreated(@RequestBody SocialEvent event) {
        newsfeedService.addPostActivity(event.getUserId(), event.getPostId());
    }

    @PostMapping("/comment-created")
    public void commentCreated(@RequestBody SocialEvent event) {
        newsfeedService.addCommentActivity(event.getUserId(), event.getPostId(), event.getCommentId());
    }

    @PostMapping("/post-liked")
    public void postLiked(@RequestBody SocialEvent event) {
        newsfeedService.addLikeActivity(event.getUserId(), event.getPostId());
    }

    @PostMapping("/post-unliked")
    public void postUnliked(@RequestBody SocialEvent event) {
        newsfeedService.addUnlikeActivity(event.getUserId(), event.getPostId());
    }

    @PostMapping("/comment-liked")
    public void commentLiked(@RequestBody SocialEvent event) {
        newsfeedService.addCommentLikeActivity(event.getUserId(), event.getPostId(), event.getCommentId());
    }

    @PostMapping("/comment-unliked")
    public void commentUnliked(@RequestBody SocialEvent event) {
        newsfeedService.removeCommentLikeActivity(event.getUserId(), event.getPostId(), event.getCommentId());
    }

    @PostMapping("/user-followed")
    public void userFollowed(@RequestBody SocialEvent event) {
        newsfeedService.addFollowActivity(event.getUserId(), event.getPostId()); // Assuming postId is used for targetUserId
    }

    @PostMapping("/follower-activity")
    public void followerActivity(@RequestBody SocialEvent event) {
        newsfeedService.addFollowerActivity(event.getType(), event.getUserId(), event.getPostId());
    }

    @PostMapping("/user-authenticated")
    public void userAuthenticated(@RequestBody UserEvent event) {
        newsfeedService.processUserenticatedUser(event.getUserId());
    }

    @PostMapping("/profile-updated")
    public void profileUpdated(@RequestBody UserEvent event) {
        newsfeedService.processProfileUpdated(event);
    }

    @PostMapping("/password-updated")
    public void passwordUpdated(@RequestBody UserEvent event) {
        newsfeedService.processPasswordUpdated(event);
    }
}