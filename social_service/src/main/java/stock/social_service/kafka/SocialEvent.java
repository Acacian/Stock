package stock.social_service.kafka;

import java.time.LocalDateTime;

public class SocialEvent {
    private String type;
    private Long userId;
    private Long targetId;
    private Long additionalId;
    private LocalDateTime timestamp;

    public SocialEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public SocialEvent(String type, Long userId, Long targetId, Long additionalId) {
        this();
        this.type = type;
        this.userId = userId;
        this.targetId = targetId;
        this.additionalId = additionalId;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getAdditionalId() { return additionalId; }
    public void setAdditionalId(Long additionalId) { this.additionalId = additionalId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}