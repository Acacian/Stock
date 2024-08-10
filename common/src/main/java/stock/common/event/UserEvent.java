package stock.common.event;

import java.io.Serializable;

public class UserEvent implements Serializable {
    private String type;
    private Long userId;
    private String data;

    public UserEvent() {}

    public UserEvent(String type, Long userId, String data) {
        this.type = type;
        this.userId = userId;
        this.data = data;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}