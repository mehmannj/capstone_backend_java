package ca.sheridancollege.odedaaja.Locker.repo;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT b FROM Booking b
        WHERE b.locker.id = :lockerId
          AND b.status IN (:statuses)
          AND (b.startDate <= :endDate AND b.endDate >= :startDate)
    """)
    List<Booking> findOverlaps(Long lockerId, LocalDate startDate, LocalDate endDate,
                               List<BookingStatus> statuses);

    List<Booking> findByUserEmailOrderByCreatedAtDesc(String email);
}
