package ca.sheridancollege.odedaaja.Locker.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.sheridancollege.odedaaja.Locker.domain.LockerLocation;

public interface LockerLocationRepository extends JpaRepository<LockerLocation, Long> {
}
