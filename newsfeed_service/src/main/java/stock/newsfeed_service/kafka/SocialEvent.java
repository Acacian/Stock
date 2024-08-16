package stock.newsfeed_service.kafka;

import java.time.LocalDateTime;

public class SocialEvent {
    private String type;
    private Long userId;
    private Long postId;
    private Long commentId;
    private LocalDateTime timestamp;

    public SocialEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public SocialEvent(String type, Long userId, Long postId, Long commentId) {
        this();
        this.type = type;
        this.userId = userId;
        this.postId = postId;
        this.commentId = commentId;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}