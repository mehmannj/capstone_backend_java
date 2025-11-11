package ca.sheridancollege.odedaaja.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.odedaaja.domain.BookedRoom;

@Repository
public interface BookedRoomRepo extends JpaRepository<BookedRoom, Long> {

	List<BookedRoom> findByBookedBy(String username);

}
