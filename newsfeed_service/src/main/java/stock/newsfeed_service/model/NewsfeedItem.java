package stock.newsfeed_service.model;

import java.time.LocalDateTime;

public class NewsfeedItem {
    private Long id;
    private String type;
    private Long userId;
    private String userName;
    private Long targetId;
    private String targetUserName;
    private String content;
    private double price;
    private double changePercentage;
    private LocalDateTime timestamp;

    // 기본 생성자
    public NewsfeedItem() {
        this.timestamp = LocalDateTime.now();
    }

    // 사용자 활동에 대한 생성자
    public NewsfeedItem(String type, Long userId, String userName, Long targetId, String targetUserName, String content) {
        this();
        this.type = type;
        this.userId = userId;
        this.userName = userName;
        this.targetId = targetId;
        this.targetUserName = targetUserName;
        this.content = content;
    }

    // 주식 가격 변동 활동에 대한 생성자
    public NewsfeedItem(String type, String stockCode, String userName, String targetUserName, double price, double changePercentage) {
        this();
        this.type = type;
        this.userName = userName;
        this.targetUserName = targetUserName;
        this.price = price;
        this.changePercentage = changePercentage;
        this.content = "Stock " + stockCode + " changed by " + changePercentage + "%";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
