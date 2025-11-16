package ca.sheridancollege.odedaaja.web.rest;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.service.ReceiptService;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/receipts")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://instimanage.netlify.app" }, allowCredentials = "true")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;
    private final BookingRepository bookingRepository;

    @PostMapping("/generate/{bookingId}")
    public ResponseEntity<?> generateReceipt(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }

            String receiptContent = receiptService.generateReceiptContent(booking);
            String receiptUrl = "/api/receipts/download/" + bookingId;
            
            return ResponseEntity.ok().body(Map.of(
                "receiptUrl", receiptUrl,
                "receiptId", "RECEIPT-" + bookingId,
                "receiptContent", receiptContent
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate receipt: " + e.getMessage()));
        }
    }

    @GetMapping("/content/{bookingId}")
    public ResponseEntity<String> getReceiptContent(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }

            String receiptContent = receiptService.generateReceiptContent(booking);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "receipt_" + bookingId + ".txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(receiptContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to generate receipt content: " + e.getMessage());
        }
    }

    @GetMapping("/download/{bookingId}")
    public ResponseEntity<String> downloadReceipt(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }

            String receiptContent = receiptService.generateReceiptContent(booking);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "receipt_" + bookingId + ".txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(receiptContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to download receipt: " + e.getMessage());
        }
    }
}
