package stock.social_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.social_service.service.SocialService;
import stock.social_service.model.Post;
import stock.social_service.kafka.SocialEvent;
import stock.social_service.model.Comment;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        return ResponseEntity.ok(socialService.createPost(post.getUserId(), post.getContent(), post.getStockId()));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        return ResponseEntity.ok(socialService.addComment(comment.getUserId(), postId, comment.getContent()));
    }

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<?> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        socialService.likePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        socialService.unlikePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        socialService.follow(followerId, followeeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        socialService.unfollow(followerId, followeeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(socialService.getPostsByUserId(userId));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(socialService.getPostById(postId));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(socialService.getCommentsByPostId(postId));
    }

    @GetMapping("/posts/{userId}/with-activity")
    public ResponseEntity<List<Post>> getPostsWithActivity(@PathVariable Long userId) {
        return ResponseEntity.ok(socialService.getPostsWithActivity(userId));
    }

    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        socialService.likeComment(userId, commentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}/likes")
    public ResponseEntity<?> unlikeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        socialService.unlikeComment(userId, commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/follower-activity")
    public ResponseEntity<List<SocialEvent>> getFollowerActivity(@PathVariable Long userId) {
        List<SocialEvent> followerActivity = socialService.getFollowerActivity(userId);
        return ResponseEntity.ok(followerActivity);
    }

    @GetMapping("/posts/stock/{stockId}")
    public ResponseEntity<Page<Post>> getPostsByStock(
            @PathVariable Long stockId,
            Pageable pageable) {
        return ResponseEntity.ok(socialService.getPostsByStock(stockId, pageable));
    }

    @GetMapping("/posts/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam String query,
            @RequestParam List<Long> userIds,
            Pageable pageable) {
        return ResponseEntity.ok(socialService.searchPosts(query, userIds, pageable));
    }
}