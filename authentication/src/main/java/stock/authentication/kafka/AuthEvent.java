package stock.authentication.kafka;

public class AuthEvent {
    private String type;
    private Long userId;

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
}