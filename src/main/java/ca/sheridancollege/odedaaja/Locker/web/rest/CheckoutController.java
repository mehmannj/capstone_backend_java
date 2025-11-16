package ca.sheridancollege.odedaaja.Locker.web.rest;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.domain.BookingStatus;
import ca.sheridancollege.odedaaja.Locker.domain.DurationOption;
import ca.sheridancollege.odedaaja.Locker.domain.Locker;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import ca.sheridancollege.odedaaja.Locker.repo.LockerRepository;
import ca.sheridancollege.odedaaja.Locker.service.ContractService;
import ca.sheridancollege.odedaaja.Locker.service.ReceiptService;
import ca.sheridancollege.odedaaja.Locker.web.dto.CheckoutRequest;
import ca.sheridancollege.odedaaja.Locker.web.dto.CheckoutSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173","https://instimanage.netlify.app" }, allowCredentials = "true")
@RequiredArgsConstructor
public class CheckoutController {

    private final LockerRepository lockerRepo;
    private final BookingRepository bookingRepo;
    private final ReceiptService receiptService;
    private final ContractService contractService;

    @PostMapping("/start")
    public ResponseEntity<CheckoutSummary> startCheckout(@RequestBody CheckoutRequest request) {
        try {
            // Validate locker exists and is available
            Locker locker = lockerRepo.findById(request.lockerId())
                    .orElseThrow(() -> new RuntimeException("Locker not found"));

            if (!locker.isActive() || !locker.isOnline()) {
                throw new RuntimeException("Locker is not available for booking");
            }

            // Calculate pricing based on duration (quarterly price = base price)
            BigDecimal quarterlyPrice = locker.getBasePrice();
            BigDecimal basePrice = quarterlyPrice.multiply(BigDecimal.valueOf(getQuarterlyMultiplier(request.duration())));
            BigDecimal tax = basePrice.multiply(BigDecimal.valueOf(0.13)); // 13% tax
            BigDecimal fees = BigDecimal.valueOf(5.00); // $5 processing fee
            BigDecimal total = basePrice.add(tax).add(fees);

            // Calculate end date based on duration
            LocalDate endDate = request.startDate().plusMonths(request.duration().getMonths());

            // Create booking with PENDING status
            Booking booking = Booking.builder()
                    .locker(locker)
                    .userEmail(request.userEmail())
                    .userName(request.userName())
                    .duration(request.duration())
                    .startDate(request.startDate())
                    .endDate(endDate)
                    .priceSubTotal(basePrice)
                    .taxAmount(tax)
                    .feesAmount(fees)
                    .totalAmount(total)
                    .status(BookingStatus.PENDING)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            booking = bookingRepo.save(booking);

            // Create checkout summary
            CheckoutSummary summary = new CheckoutSummary(
                    booking.getId(),
                    locker.getLocation().getBuilding(),
                    locker.getLocation().getFloor(),
                    locker.getLockerNumber(),
                    request.duration().name(),
                    request.startDate(),
                    endDate,
                    basePrice,
                    tax,
                    fees,
                    total,
                    "mock_payment_intent_" + booking.getId(),
                    "pending"
            );

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            throw new RuntimeException("Failed to start checkout: " + e.getMessage());
        }
    }

    @PostMapping("/mock-payment/{bookingId}")
    public ResponseEntity<Map<String, Object>> processMockPayment(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepo.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new RuntimeException("Booking is not in pending status");
            }

            // Update booking status to PAID
            booking.setStatus(BookingStatus.PAID);
            booking.setPaymentProvider("mock");
            booking.setPaymentReference("mock_payment_" + bookingId);
            booking.setUpdatedAt(OffsetDateTime.now());

            // Generate receipt and contract
            String receiptUrl = receiptService.generateReceipt(booking);
            String contractUrl = contractService.generateContract(booking);
            booking.setReceiptUrl(receiptUrl);
            booking.setContractUrl(contractUrl);

            booking = bookingRepo.save(booking);

            // Mark locker as offline (booked)
            Locker locker = booking.getLocker();
            locker.setOnline(false);
            lockerRepo.save(locker);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "bookingId", bookingId,
                    "receiptUrl", receiptUrl,
                    "contractUrl", contractUrl,
                    "message", "Payment processed successfully"
            ));

        } catch (Exception e) {
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    private int getQuarterlyMultiplier(DurationOption duration) {
        return switch (duration) {
            case QUARTERLY -> 1; // 1 quarter = base price
            case TWO_QUARTERS -> 2; // 2 quarters = 2x base price
            case YEARLY -> 3; // 1 year = 3 quarters = 3x base price
        };
    }
}
