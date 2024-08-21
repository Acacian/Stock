package stock.newsfeed_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
}