package ca.sheridancollege.odedaaja.Locker.web.rest;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.domain.BookingStatus;
import ca.sheridancollege.odedaaja.Locker.domain.Locker;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import ca.sheridancollege.odedaaja.Locker.repo.LockerRepository;
import ca.sheridancollege.odedaaja.Locker.service.ContractService;
import ca.sheridancollege.odedaaja.Locker.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LockerPublicController {

    private final LockerRepository lockerRepo;
    private final BookingRepository bookingRepo;
    private final ContractService contractService;
    private final ReceiptService receiptService;

    // ✅ Get all available lockers
    @GetMapping("/lockers")
    public List<Locker> getAvailableLockers() {
        return lockerRepo.findAll().stream()
                .filter(l -> l.isActive() && l.isOnline())
                .toList();
    }

    // ✅ Book a locker (by email + name)
    @PostMapping("/bookings")
    public Booking bookLocker(
            @RequestParam Long lockerId,
            @RequestParam String userEmail,
            @RequestParam String userName,
            @RequestParam int durationMonths
    ) {
        var locker = lockerRepo.findById(lockerId).orElseThrow();

        BigDecimal base = locker.getBasePrice();
        BigDecimal tax = base.multiply(BigDecimal.valueOf(0.13)); // 13% tax
        BigDecimal fees = BigDecimal.valueOf(5);                  // mock service fee
        BigDecimal total = base.add(tax).add(fees);

        Booking booking = Booking.builder()
                .locker(locker)
                .userEmail(userEmail)
                .userName(userName)
                .status(BookingStatus.PAID) // PAID for now since mock payment
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(durationMonths))
                .priceSubTotal(base)
                .taxAmount(tax)
                .feesAmount(fees)
                .totalAmount(total)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        booking = bookingRepo.save(booking);

        // 🔹 Generate and set receipt + contract URLs
        String receiptUrl = receiptService.generateReceipt(booking);
        String contractUrl = contractService.generateContract(booking);
        booking.setReceiptUrl(receiptUrl);
        booking.setContractUrl(contractUrl);

        return bookingRepo.save(booking);
    }

    // ✅ My bookings (by user email)
    @GetMapping("/users/{email}/bookings")
    public List<Booking> getUserBookings(@PathVariable String email) {
        return bookingRepo.findAll().stream()
                .filter(b -> b.getUserEmail().equalsIgnoreCase(email))
                .toList();
    }

    // ✅ Request cancellation (requires admin approval)
    @PatchMapping("/bookings/{id}/request-cancellation")
    public Map<String, Object> requestCancellation(@PathVariable Long id, 
                                                   @RequestParam String reason) {
        var booking = bookingRepo.findById(id).orElseThrow();
        
        if (booking.getStatus() != BookingStatus.PAID) {
            throw new RuntimeException("Only paid bookings can request cancellation");
        }
        
        booking.setStatus(BookingStatus.PENDING_CANCELLATION);
        booking.setCancellationReason(reason);
        booking.setCancellationRequestDate(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());
        
        booking = bookingRepo.save(booking);
        
        return Map.of(
            "success", true,
            "message", "Cancellation request submitted. Awaiting admin approval.",
            "bookingId", booking.getId()
        );
    }

    // ✅ Extend booking
    @PatchMapping("/bookings/{id}/extend")
    public Booking extendBooking(
            @PathVariable Long id,
            @RequestParam int months
    ) {
        var booking = bookingRepo.findById(id).orElseThrow();
        booking.setEndDate(booking.getEndDate().plusMonths(months));
        booking.setUpdatedAt(OffsetDateTime.now());

        // regenerate updated receipt (optional)
        String newReceipt = receiptService.generateReceipt(booking);
        booking.setReceiptUrl(newReceipt);

        return bookingRepo.save(booking);
    }
}
