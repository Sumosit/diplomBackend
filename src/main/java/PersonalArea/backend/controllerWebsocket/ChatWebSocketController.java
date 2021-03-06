package PersonalArea.backend.controllerWebsocket;

import PersonalArea.backend.Entity.ChatId;
import PersonalArea.backend.Entity.User;
import PersonalArea.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatWebSocketController {

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatIdRepository chatIdRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat/{id}")
    @SendTo("/topic/chat/{id}")
    public ChatMessage greeting(ChatRestForm chatRestForm) throws Exception {
        Thread.sleep(1000);
        try {
            ChatMessage chatMessage =
                    new ChatMessage(
                            null,
                            chatRestForm.getMessage(),
                            userRepository.findUserById(chatRestForm.getUserId()),
                            chatRestForm.getSendDate(),
                            "False"
                    );
            chatMessageRepository.save(chatMessage);
            ChatId chatId = chatIdRepository.findChatIdById(chatRestForm.getChatId());
            Set<ChatMessage> chatMessages = chatId.getChatMessageSet();
            chatId.setLastId(chatMessage.getId());
            chatMessages.add(chatMessage);
            chatIdRepository.save(chatId);
            return chatMessage;
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/api/chat/add")
    public String createChat(@RequestParam Long id1, Long id2) {
        try {
            User sender = userRepository.findUserById(id1);
            User rec = userRepository.findUserById(id2);
            if (!chatIdRepository.existsAllBySenderAndRecipient(sender, rec)
                    && !chatIdRepository.existsAllByRecipientAndSender(sender, rec)) {
                chatIdRepository.save(new ChatId(
                        null,
                        sender,
                        rec,
                        null,
                        null));
                return "True";
            } else {
                return "This chat already exists";
            }
        } catch (Exception e) {
            return "Error";
        }
    }

    @GetMapping("/api/chat/one/{chatId}")
    public ChatId getOneChatById(@PathVariable Long chatId) {
        ChatId chat = chatIdRepository.findChatIdById(chatId);
        for (ChatMessage chatMessage : chat.getChatMessageSet()) {
            chatMessage.setChecked("True");
        }
        chatIdRepository.save(chat);
        return chat;
    }

    @GetMapping("/api/chat/messages/{chatId}")
    public Set<ChatMessage> getOneChatMessagesById(@PathVariable Long chatId) {
        ChatId chat = chatIdRepository.findChatIdById(chatId);
        for (ChatMessage chatMessage : chat.getChatMessageSet()) {
            chatMessage.setChecked("True");
        }
        chatIdRepository.save(chat);
        return chat.getChatMessageSet();
    }

    @GetMapping("/api/chat/all/{user_id}")
    public List<ChatId> getAllChatById(@PathVariable Long user_id) {
        User user = userRepository.findUserById(user_id);
        return chatIdRepository.findAllBySenderOrRecipient(user, user);
    }
}
