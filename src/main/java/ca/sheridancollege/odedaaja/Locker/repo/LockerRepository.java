package ca.sheridancollege.odedaaja.Locker.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.sheridancollege.odedaaja.Locker.domain.Locker;

public interface LockerRepository extends JpaRepository<Locker, Long> {
    List<Locker> findByActiveTrueAndOnlineTrue();
}
