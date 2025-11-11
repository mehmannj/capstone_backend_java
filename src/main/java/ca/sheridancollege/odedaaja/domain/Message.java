package ca.sheridancollege.odedaaja.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sends the message
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Users sender;

    // The user who receives the message
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private Users receiver;

    // The message content
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    // Optional: for "seen" status
    @Column(name = "is_read")
    private boolean isRead;
}
