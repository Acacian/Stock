package stock.social_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import stock.social_service.client.UserServiceClient;
import stock.social_service.dto.UserDto;
import stock.social_service.model.WebRTCMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebRTCController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserServiceClient userServiceClient;
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    @Autowired
    public WebRTCController(SimpMessagingTemplate messagingTemplate, UserServiceClient userServiceClient) {
        this.messagingTemplate = messagingTemplate;
        this.userServiceClient = userServiceClient;
    }

    @MessageMapping("/webrtc.connect")
    public void connect(@Payload WebRTCMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserDto user = userServiceClient.getUserById(message.getSenderId());
        
        if (user != null) {
            userSessions.put(user.getId().toString(), sessionId);
            messagingTemplate.convertAndSendToUser(message.getReceiverId(), "/queue/webrtc", message);
        }
    }

    @MessageMapping("/webrtc.message")
    public void handleWebRTCMessage(@Payload WebRTCMessage message) {
        String recipientSessionId = userSessions.get(message.getReceiverId());
        if (recipientSessionId != null) {
            messagingTemplate.convertAndSendToUser(recipientSessionId, "/queue/webrtc", message);
        }
    }

    @MessageMapping("/webrtc.disconnect")
    public void disconnect(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
    }
}