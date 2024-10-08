package stock.social_service.model;

import java.time.Instant;

public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    private Instant timestamp;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    // Getters and setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}