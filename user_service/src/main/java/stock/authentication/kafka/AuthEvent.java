package stock.user_service.kafka;

public class AuthEvent {
    private String type;
    private Long userId;
    private String email;

    // Constructor
    public AuthEvent(String type, Long userId) {
        this.type = type;
        this.userId = userId;
    }

    // Getters and setters
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

    public AuthEvent(String type, Long userId, String email) {
        this.type = type;
        this.userId = userId;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}