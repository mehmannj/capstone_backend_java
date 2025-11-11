package ca.sheridancollege.odedaaja.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ca.sheridancollege.odedaaja.domain.ResourceRental;
import java.util.List;

public interface ResourceRentalRepository extends JpaRepository<ResourceRental, Long> {
    
    // Find rentals by student
    List<ResourceRental> findByStudentIdOrderByRequestedAtDesc(Long studentId);
    
    // Find rentals by status
    List<ResourceRental> findByStatusOrderByRequestedAtDesc(ResourceRental.RentalStatus status);
    
    // Find rentals by resource
    List<ResourceRental> findByResourceIdOrderByRequestedAtDesc(Long resourceId);
    
    // Find pending rentals for coordinator approval
    @Query("SELECT rr FROM ResourceRental rr WHERE rr.status = 'PENDING' ORDER BY rr.requestedAt ASC")
    List<ResourceRental> findPendingRentals();
    
    // Find rentals by department (coordinator view)
    @Query("SELECT rr FROM ResourceRental rr WHERE rr.resource.department.id = :departmentId ORDER BY rr.requestedAt DESC")
    List<ResourceRental> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    // Find active rentals (picked up but not returned)
    @Query("SELECT rr FROM ResourceRental rr WHERE rr.status IN ('APPROVED', 'PICKED_UP') ORDER BY rr.pickupTime ASC")
    List<ResourceRental> findActiveRentals();
    
    // Find overdue rentals
    @Query("SELECT rr FROM ResourceRental rr WHERE rr.status = 'PICKED_UP' AND rr.returnTime < CURRENT_TIMESTAMP ORDER BY rr.returnTime ASC")
    List<ResourceRental> findOverdueRentals();
    
    // Check if resource is available for rental period
    @Query("SELECT COUNT(rr) FROM ResourceRental rr WHERE rr.resource.id = :resourceId " +
           "AND rr.status IN ('APPROVED', 'PICKED_UP') " +
           "AND ((:startTime <= rr.pickupTime AND :endTime >= rr.pickupTime) OR " +
           "(:startTime <= rr.returnTime AND :endTime >= rr.returnTime) OR " +
           "(:startTime >= rr.pickupTime AND :endTime <= rr.returnTime))")
    int countConflictingRentals(@Param("resourceId") Long resourceId, 
                                @Param("startTime") java.time.LocalDateTime startTime, 
                                @Param("endTime") java.time.LocalDateTime endTime);
}
