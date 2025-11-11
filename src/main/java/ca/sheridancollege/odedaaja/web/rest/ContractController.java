package ca.sheridancollege.odedaaja.web.rest;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.service.ContractService;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final BookingRepository bookingRepository;

    @PostMapping("/generate/{bookingId}")
    public ResponseEntity<?> generateContract(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }

            String contractContent = contractService.generateContractContent(booking);
            String contractUrl = "/api/contracts/download/" + bookingId;
            
            return ResponseEntity.ok().body(Map.of(
                "contractUrl", contractUrl,
                "contractId", "CONTRACT-" + bookingId,
                "contractContent", contractContent
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate contract: " + e.getMessage()));
        }
    }

    @GetMapping("/content/{bookingId}")
    public ResponseEntity<String> getContractContent(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }

            String contractContent = contractService.generateContractContent(booking);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "contract_" + bookingId + ".txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(contractContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to generate contract content: " + e.getMessage());
        }
    }

    @GetMapping("/download/{bookingId}")
    public ResponseEntity<String> downloadContract(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }

            String contractContent = contractService.generateContractContent(booking);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "contract_" + bookingId + ".txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(contractContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to download contract: " + e.getMessage());
        }
    }
}
