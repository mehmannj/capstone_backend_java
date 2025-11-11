package ca.sheridancollege.odedaaja.web.rest;

import ca.sheridancollege.odedaaja.domain.Notification;
import ca.sheridancollege.odedaaja.repo.NotificationRepository;
import ca.sheridancollege.odedaaja.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationRepo.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepo.findByUserIdOrderByTimestampDesc(userId);
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepo.save(notification);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return notificationRepo.findById(id)
                .map(notification -> {
                    notification.setRead(true);
                    return ResponseEntity.ok(notificationRepo.save(notification));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long userId) {
        List<Notification> notifications = notificationRepo.findByUserIdAndReadFalse(userId);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepo.saveAll(notifications);
        
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        if (notificationRepo.existsById(id)) {
            notificationRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(@PathVariable Long userId) {
        int count = notificationRepo.countByUserIdAndReadFalse(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}

