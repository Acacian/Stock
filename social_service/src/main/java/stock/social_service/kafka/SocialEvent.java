package stock.social_service.kafka;

public class SocialEvent {
    private String type;
    private Long userId;
    private Long targetId;

    // Constructor
    public SocialEvent(String type, Long userId, Long targetId) {
        this.type = type;
        this.userId = userId;
        this.targetId = targetId;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}