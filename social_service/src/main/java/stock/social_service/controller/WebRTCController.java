package stock.social_service.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import stock.social_service.model.WebRTCMessage;

@Controller
public class WebRTCController {

    @MessageMapping("/webrtc.offer")
    @SendTo("/topic/webrtc")
    public WebRTCMessage offer(WebRTCMessage message) {
        return message;
    }

    @MessageMapping("/webrtc.answer")
    @SendTo("/topic/webrtc")
    public WebRTCMessage answer(WebRTCMessage message) {
        return message;
    }

    @MessageMapping("/webrtc.iceCandidate")
    @SendTo("/topic/webrtc")
    public WebRTCMessage iceCandidate(WebRTCMessage message) {
        return message;
    }
}
