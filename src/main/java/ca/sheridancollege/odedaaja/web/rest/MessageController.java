package ca.sheridancollege.odedaaja.web.rest;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.sheridancollege.odedaaja.service.MessageService;
import ca.sheridancollege.odedaaja.web.rest.MessageDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor       // Lombok generates a constructor with MessageService
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://instimanage.netlify.app" })

public class MessageController {

    // ✅ Let Spring inject this — no manual "new"
    private final MessageService messageService;

    @PostMapping
    public MessageDTO sendMessage(@RequestBody MessageDTO dto) {
        return messageService.sendMessage(dto);
    }

    @GetMapping("/{user1Id}/{user2Id}")
    public List<MessageDTO> getMessages(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id) {
        return messageService.getMessagesBetween(user1Id, user2Id);
    }
}
