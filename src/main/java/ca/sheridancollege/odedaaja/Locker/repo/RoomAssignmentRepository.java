package ca.sheridancollege.odedaaja.Locker.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.sheridancollege.odedaaja.Locker.domain.RoomAssignment;

public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, Long> {
}
