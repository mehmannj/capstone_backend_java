package ca.sheridancollege.odedaaja.repo;

import ca.sheridancollege.odedaaja.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<Notification> findByUserIdAndReadFalse(Long userId);
    
    int countByUserIdAndReadFalse(Long userId);
    
    List<Notification> findByUserIdIsNullOrderByTimestampDesc(); // Global notifications
}
