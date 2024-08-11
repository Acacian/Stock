package stock.common.event;

import java.io.Serializable;

public class SocialEvent implements Serializable {
    private String type;
    private Long userId;
    private Long targetId;

    public SocialEvent() {}

    public SocialEvent(String type, Long userId, Long targetId) {
        this.type = type;
        this.userId = userId;
        this.targetId = targetId;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    @Override
    public String toString() {
        return "SocialEvent{" +
                "type='" + type + '\'' +
                ", userId=" + userId +
                ", targetId=" + targetId +
                '}';
    }
}