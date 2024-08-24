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
}
