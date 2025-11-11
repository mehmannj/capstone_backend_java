package ca.sheridancollege.odedaaja.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ca.sheridancollege.odedaaja.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}


