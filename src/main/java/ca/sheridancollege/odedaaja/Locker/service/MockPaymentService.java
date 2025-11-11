package ca.sheridancollege.odedaaja.Locker.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ca.sheridancollege.odedaaja.Locker.domain.BookingStatus;
import ca.sheridancollege.odedaaja.Locker.domain.Payment;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import ca.sheridancollege.odedaaja.Locker.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockPaymentService implements PaymentService {
    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;

    @Override
    public String createPaymentIntent(java.math.BigDecimal amount, String currency, String customerEmail) {
        return "mock_pi_" + UUID.randomUUID();
    }

    @Override
    public void markPaid(Long bookingId, String providerPaymentId) {
        var booking = bookingRepo.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.PAID);
        booking.setPaymentProvider("MOCK");
        booking.setPaymentReference(providerPaymentId);
        booking.setUpdatedAt(OffsetDateTime.now());
        bookingRepo.save(booking);

        paymentRepo.save(Payment.builder()
                .bookingId(bookingId)
                .provider("MOCK")
                .providerPaymentId(providerPaymentId)
                .status("SUCCEEDED")
                .amount(booking.getTotalAmount())
                .createdAt(OffsetDateTime.now())
                .build());
    }

    @Override
    public void markFailed(Long bookingId, String errorMessage) {
        var booking = bookingRepo.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.FAILED);
        booking.setUpdatedAt(OffsetDateTime.now());
        bookingRepo.save(booking);

        paymentRepo.save(Payment.builder()
                .bookingId(bookingId)
                .provider("MOCK")
                .providerPaymentId("failed_" + bookingId)
                .status("FAILED")
                .amount(booking.getTotalAmount())
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
