package stock.social_service.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import stock.social_service.model.ChatMessage;
import stock.social_service.service.ChatMessagePublisher;

@Controller
public class ChatController {

    private final ChatMessagePublisher messagePublisher;

    public ChatController(ChatMessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        messagePublisher.publish(chatMessage);
        return chatMessage;
    }
}
