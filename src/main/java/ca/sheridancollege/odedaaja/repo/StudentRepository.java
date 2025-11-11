package ca.sheridancollege.odedaaja.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ca.sheridancollege.odedaaja.domain.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}


