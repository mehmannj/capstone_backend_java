package ca.sheridancollege.odedaaja.service;

import ca.sheridancollege.odedaaja.domain.Message;
import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.repo.MessageRepository;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import ca.sheridancollege.odedaaja.web.rest.MessageDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepo;
    private final UserRepo userRepo;

    public MessageService(MessageRepository messageRepo, UserRepo userRepo) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    public MessageDTO sendMessage(MessageDTO dto) {
        // Validate sender and receiver exist
        Users sender = userRepo.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        Users receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Create and save message
        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(dto.getContent())
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        message = messageRepo.save(message);

        // Convert to DTO and return
        return new MessageDTO(
                message.getSender().getId(),
                message.getReceiver().getId(),
                message.getContent(),
                message.getTimestamp()
        );
    }

    public List<MessageDTO> getMessagesBetween(Long user1Id, Long user2Id) {
        // Validate users exist
        userRepo.findById(user1Id).orElseThrow(() -> new RuntimeException("User 1 not found"));
        userRepo.findById(user2Id).orElseThrow(() -> new RuntimeException("User 2 not found"));

        // Get messages between the two users (bidirectional)
        List<Message> messages = messageRepo.findMessagesBetweenUsers(user1Id, user2Id);

        // Mark messages as read for the requesting user
        messages.stream()
                .filter(msg -> msg.getReceiver().getId().equals(user1Id))
                .forEach(msg -> {
                    msg.setRead(true);
                    messageRepo.save(msg);
                });

        return messages.stream()
                .map(msg -> new MessageDTO(
                        msg.getSender().getId(),
                        msg.getReceiver().getId(),
                        msg.getContent(),
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getUnreadMessages(Long userId) {
        List<Message> unreadMessages = messageRepo.findUnreadMessagesByReceiverId(userId);
        
        return unreadMessages.stream()
                .map(msg -> new MessageDTO(
                        msg.getSender().getId(),
                        msg.getReceiver().getId(),
                        msg.getContent(),
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    public int getUnreadMessageCount(Long userId) {
        return messageRepo.countUnreadMessagesByReceiverId(userId);
    }
}