package stock.newsfeed_service.kafka;

public class UserEvent {
    private String type;
    private Long userId;
    private Long targetUserId;

    // Constructors, getters, and setters
    public UserEvent() {}

    public UserEvent(String type, Long userId, Long targetUserId) {
        this.type = type;
        this.userId = userId;
        this.targetUserId = targetUserId;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
}