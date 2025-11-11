package ca.sheridancollege.odedaaja.Locker.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.sheridancollege.odedaaja.Locker.domain.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
