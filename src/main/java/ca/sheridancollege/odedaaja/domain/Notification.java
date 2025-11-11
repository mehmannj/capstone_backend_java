package ca.sheridancollege.odedaaja.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "notification",
    indexes = {
        @Index(name = "idx_notification_user", columnList = "userId"),
        @Index(name = "idx_notification_read", columnList = "`read`")
    }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String message;
    private LocalDateTime timestamp;

    @Column(name = "`read`", nullable = false)
    private boolean read = false;

    private Long userId;
    
    // Fields for MESSAGE type notifications
    private String senderUsername;
    private Long senderId;

    public enum NotificationType {
        ROOM_POST, ROOM_REQUEST, MESSAGE, SYSTEM, INFO, SUCCESS, WARNING, ERROR
    }
}
