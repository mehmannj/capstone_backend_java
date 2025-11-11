package ca.sheridancollege.odedaaja.service;

import java.util.List;
import ca.sheridancollege.odedaaja.domain.Notification;

public interface NotificationService {
    List<Notification> getUserNotifications(Long userId);
    Notification createNotification(Notification notification);
    Notification markAsRead(Long notificationId);
    void deleteNotification(Long notificationId);
    void createRoomPostNotification(String roomNumber, String postedBy);
    void createRoomRequestNotification(String description, String requestedBy);
    void createMessageNotification(String message, String sender);
}



