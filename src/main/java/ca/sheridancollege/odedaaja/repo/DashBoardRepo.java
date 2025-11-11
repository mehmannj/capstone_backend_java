package ca.sheridancollege.odedaaja.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.odedaaja.domain.DashBoard;
import ca.sheridancollege.odedaaja.domain.Users;

@Repository
public interface DashBoardRepo extends JpaRepository<DashBoard, Long> {

	List<DashBoard> findByUsersUsername(String username);
	
	List<DashBoard> findByUsersRole(Users.Role role);

}
