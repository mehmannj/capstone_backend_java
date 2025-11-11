package ca.sheridancollege.odedaaja.repo;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.odedaaja.domain.RoomPost;

@Repository
public interface RoomPostRepository extends JpaRepository<RoomPost, Long> {



	List<RoomPost> findByUsersUsername(String username);
	List<RoomPost> findByPostedBy(String postedBy);
	
	// Time-based filtering methods
	List<RoomPost> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
	List<RoomPost> findByDateLessThanOrderByDateDesc(LocalDate date);
	
}
