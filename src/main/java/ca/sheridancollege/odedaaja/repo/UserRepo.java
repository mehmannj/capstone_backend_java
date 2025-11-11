package ca.sheridancollege.odedaaja.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.odedaaja.domain.Users;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {
	   Optional<Users> findByUsername(String username);
	    List<Users> findByRole(Users.Role role);   
}