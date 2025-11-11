package ca.sheridancollege.odedaaja.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.odedaaja.domain.RoomRequest;

@Repository
public interface RoomRequestRepository extends JpaRepository<RoomRequest, Long> {



	List<RoomRequest> findByUsersUsername(String username);
	List<RoomRequest> findByPostedBy(String postedBy);
	
	// Time-based filtering methods
	List<RoomRequest> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
	List<RoomRequest> findByDateLessThanOrderByDateDesc(LocalDate date);
}
