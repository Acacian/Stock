package stock.social_service.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import stock.social_service.model.ChatMessage;

@Service
public class ChatMessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void receiveMessage(ChatMessage message) {
        // 수신된 메시지를 처리하는 로직 추가
        System.out.println("Received message: " + message.getContent());

        // WebSocket을 통해 모든 구독자에게 메시지 전송
        messagingTemplate.convertAndSend("/topic/public", message);
    }
}
