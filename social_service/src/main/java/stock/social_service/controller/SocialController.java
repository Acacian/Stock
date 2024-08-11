package stock.social_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.social_service.service.SocialService;
import stock.social_service.model.Post;
import stock.social_service.model.Comment;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        return ResponseEntity.ok(socialService.createPost(post.getUserId(), post.getContent()));
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

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestParam Long followerId, @RequestParam Long followedId) {
        socialService.follow(followerId, followedId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(socialService.getPostsByUserId(userId));
    }
}