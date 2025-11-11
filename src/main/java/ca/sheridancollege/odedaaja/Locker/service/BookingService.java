package ca.sheridancollege.odedaaja.Locker.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.domain.BookingStatus;
import ca.sheridancollege.odedaaja.Locker.domain.DurationOption;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import ca.sheridancollege.odedaaja.Locker.repo.LockerRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepo;
    private final LockerRepository lockerRepo;
    private final PricingService pricingService;

    public List<Booking> userOrders(String email) {
        return bookingRepo.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public Booking createPendingBooking(Long lockerId, String userEmail, String userName,
                                        DurationOption duration, LocalDate startDate) {
        var locker = lockerRepo.findById(lockerId).orElseThrow();

        var months = duration.getMonths();
        var endDate = startDate.plusMonths(months).minusDays(1);

        // ✅ check overlaps for both PENDING and PAID
        var overlaps = bookingRepo.findOverlaps(
                lockerId,
                startDate,
                endDate,
                List.of(BookingStatus.PAID, BookingStatus.PENDING)
        );

        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Selected locker is not available for the chosen period.");
        }

        var price = pricingService.calculate(duration);
        var now = OffsetDateTime.now();

        var booking = Booking.builder()
                .locker(locker)
                .userEmail(userEmail)
                .userName(userName)
                .duration(duration)
                .startDate(startDate)
                .endDate(endDate)
                .priceSubTotal(price.subTotal())
                .taxAmount(price.tax())
                .feesAmount(price.fees())
                .totalAmount(price.total())
                .status(BookingStatus.PENDING) // mark pending until payment confirmed
                .createdAt(now)
                .updatedAt(now)
                .build();

        return bookingRepo.save(booking);
    }

}
