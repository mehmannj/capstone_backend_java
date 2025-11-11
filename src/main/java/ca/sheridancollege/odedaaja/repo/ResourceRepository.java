package ca.sheridancollege.odedaaja.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ca.sheridancollege.odedaaja.domain.Resource;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    // Find available resources
    List<Resource> findByAvailableTrueAndActiveTrue();
    
    // Find resources by category
    List<Resource> findByCategoryAndAvailableTrueAndActiveTrue(String category);
    
    // Find resources by department
    List<Resource> findByDepartmentIdAndAvailableTrueAndActiveTrue(Long departmentId);
    
    // Search resources by name or description
    @Query("SELECT r FROM Resource r WHERE (r.name LIKE %:searchTerm% OR r.description LIKE %:searchTerm%) AND r.available = true AND r.active = true")
    List<Resource> searchAvailableResources(@Param("searchTerm") String searchTerm);
    
    // Find resources by multiple criteria
    @Query("SELECT r FROM Resource r WHERE r.available = true AND r.active = true " +
           "AND (:category IS NULL OR r.category = :category) " +
           "AND (:departmentId IS NULL OR r.department.id = :departmentId) " +
           "AND (:maxPrice IS NULL OR r.rentalPricePerDay <= :maxPrice)")
    List<Resource> findAvailableResourcesByCriteria(
        @Param("category") String category,
        @Param("departmentId") Long departmentId,
        @Param("maxPrice") java.math.BigDecimal maxPrice
    );
}
