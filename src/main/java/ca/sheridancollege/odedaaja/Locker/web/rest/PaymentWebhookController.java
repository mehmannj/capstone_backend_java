package ca.sheridancollege.odedaaja.Locker.web.rest;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import ca.sheridancollege.odedaaja.Locker.service.ContractService;
import ca.sheridancollege.odedaaja.Locker.service.EmailService;
import ca.sheridancollege.odedaaja.Locker.service.PaymentService;
import ca.sheridancollege.odedaaja.Locker.service.ReceiptService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {
    private final PaymentService paymentService;
    private final BookingRepository bookingRepo;
    private final ContractService contractService;
    private final ReceiptService receiptService;
    private final EmailService emailService;

    @PostMapping("/mock/succeed")
    public Map<String,Object> mockSucceed(@RequestParam Long bookingId) {
        try {
            var providerPaymentId = "mock_ch_" + bookingId;
            paymentService.markPaid(bookingId, providerPaymentId);

            Booking booking = bookingRepo.findById(bookingId).orElseThrow();
            String receiptPath = receiptService.generateReceipt(booking);
            String contractPath = contractService.generateContract(booking);
            booking.setReceiptUrl(receiptPath);
            booking.setContractUrl(contractPath);
            bookingRepo.save(booking);

            emailService.sendReceipt(booking.getUserEmail(), booking.getId(), receiptPath);
            emailService.sendContract(booking.getUserEmail(), booking.getId(), contractPath);

            return Map.of(
                "status", "success", 
                "bookingId", bookingId, 
                "receipt", receiptPath,
                "contract", contractPath,
                "message", "Payment processed successfully"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error", 
                "message", "Payment processing failed: " + e.getMessage()
            );
        }
    }
}
