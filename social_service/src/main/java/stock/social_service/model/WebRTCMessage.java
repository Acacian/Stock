package stock.social_service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebRTCMessage {
    private String type;
    private String sdp;
    private String candidate;
    private String sender;
    private String receiver;

    // sender와 receiver를 getSenderId와 getReceiverId로 사용
    public String getSenderId() {
        return sender;
    }

    public String getReceiverId() {
        return receiver;
    }
}