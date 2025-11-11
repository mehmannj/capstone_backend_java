package ca.sheridancollege.odedaaja.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ca.sheridancollege.odedaaja.domain.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}


