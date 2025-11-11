package ca.sheridancollege.odedaaja.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ca.sheridancollege.odedaaja.domain.Program;

public interface ProgramRepository extends JpaRepository<Program, Long> {
}


