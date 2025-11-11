package ca.sheridancollege.odedaaja.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ca.sheridancollege.odedaaja.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}


