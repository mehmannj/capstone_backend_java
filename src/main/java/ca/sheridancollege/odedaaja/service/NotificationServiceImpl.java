package ca.sheridancollege.odedaaja.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ca.sheridancollege.odedaaja.domain.Notification;
import ca.sheridancollege.odedaaja.domain.Notification.NotificationType;
import ca.sheridancollege.odedaaja.repo.NotificationRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    @Override
    public Notification createNotification(Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }
    
    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
    
    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    @Override
    public void createRoomPostNotification(String roomNumber, String postedBy) {
        Notification notification = new Notification();
        notification.setUserId(1L); // Admin user ID - you might want to make this configurable
        notification.setTitle("New Room Posted");
        notification.setMessage("Room " + roomNumber + " has been posted by " + postedBy);
        notification.setType(NotificationType.ROOM_POST);
        createNotification(notification);
    }
    
    @Override
    public void createRoomRequestNotification(String description, String requestedBy) {
        Notification notification = new Notification();
        notification.setUserId(1L); // Admin user ID - you might want to make this configurable
        notification.setTitle("New Room Request");
        notification.setMessage("Room request: " + description + " by " + requestedBy);
        notification.setType(NotificationType.ROOM_REQUEST);
        createNotification(notification);
    }
    
    @Override
    public void createMessageNotification(String message, String sender) {
        Notification notification = new Notification();
        notification.setUserId(1L); // Admin user ID - you might want to make this configurable
        notification.setTitle("New Message");
        notification.setMessage("Message from " + sender + ": " + message);
        notification.setType(NotificationType.MESSAGE);
        createNotification(notification);
    }
}
