package ca.sheridancollege.odedaaja.web.rest;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;
}
