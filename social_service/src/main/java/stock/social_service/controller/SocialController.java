package stock.social_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.social_service.model.Post;
import stock.social_service.model.Comment;
import stock.social_service.service.SocialService;
import stock.social_service.kafka.SocialEvent;
import stock.social_service.dto.CreatePostRequest;
import stock.social_service.dto.CreateCommentRequest;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request) {
        Post post = socialService.createPost(request.getUserId(), request.getContent(), request.getStockId());
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
        Comment comment = socialService.addComment(request.getUserId(), postId, request.getContent());
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        socialService.likePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        socialService.unlikePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/follow")
    public ResponseEntity<Void> follow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        socialService.follow(followerId, followeeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/follow")
    public ResponseEntity<Void> unfollow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        socialService.unfollow(followerId, followeeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<Page<Post>> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return ResponseEntity.ok(socialService.getPostsByUserId(userId, pageable));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(socialService.getPostById(postId));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(socialService.getCommentsByPostId(postId));
    }

    @GetMapping("/posts/activity/{userId}")
    public ResponseEntity<Page<Post>> getPostsWithActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return ResponseEntity.ok(socialService.getPostsWithActivity(userId, pageable));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        socialService.likeComment(userId, commentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        socialService.unlikeComment(userId, commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/follower-activity/{userId}")
    public ResponseEntity<List<SocialEvent>> getFollowerActivity(@PathVariable Long userId) {
        return ResponseEntity.ok(socialService.getFollowerActivity(userId));
    }

    @GetMapping("/posts/stock/{stockId}")
    public ResponseEntity<Page<Post>> getPostsByStock(
            @PathVariable Long stockId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return ResponseEntity.ok(socialService.getPostsByStock(stockId, pageable));
    }

    @GetMapping("/posts/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam String query,
            @RequestParam(required = false) Long stockId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return ResponseEntity.ok(socialService.searchPosts(query, stockId, pageable));
    }
}