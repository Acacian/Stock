package stock.user_service.kafka;

public class UserEvent {
    private String type;
    private Long userId;
    private String email;

    public UserEvent() {}

    public UserEvent(String type, Long userId, String email) {
        this.type = type;
        this.userId = userId;
        this.email = email;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}