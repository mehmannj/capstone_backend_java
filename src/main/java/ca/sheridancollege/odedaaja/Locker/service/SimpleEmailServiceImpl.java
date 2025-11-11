package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.EmailStatus;
import ca.sheridancollege.odedaaja.Locker.domain.EmailType;
import ca.sheridancollege.odedaaja.Locker.domain.NotificationLog;
import ca.sheridancollege.odedaaja.Locker.repo.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimpleEmailServiceImpl implements EmailService {
    
    private final NotificationLogRepository notificationLogRepo;

    @Override
    public EmailStatus sendReceipt(String userEmail, Long bookingId, String receiptUrl) {
        return send(userEmail, bookingId, EmailType.RECEIPT, "Locker Rental Receipt", receiptUrl);
    }

    @Override
    public EmailStatus sendContract(String userEmail, Long bookingId, String contractUrl) {
        return send(userEmail, bookingId, EmailType.CONTRACT, "Locker Rental Contract", contractUrl);
    }

    private EmailStatus send(String userEmail, Long bookingId, EmailType type, String subject, String url) {
        EmailStatus status = EmailStatus.SENT;
        String providerId = UUID.randomUUID().toString();
        String errorMessage = null;
        
        try {
            // In a real implementation, this would send an actual email
            // For now, we'll just log it
            System.out.println("Email sent to " + userEmail + ": " + subject + " - " + url);
        } catch (Exception ex) {
            status = EmailStatus.FAILED;
            errorMessage = ex.getMessage();
            System.err.println("Failed to send email: " + ex.getMessage());
        }

        // Log the notification
        NotificationLog log = NotificationLog.builder()
                .bookingId(bookingId)
                .type(type)
                .status(status)
                .providerMessageId(providerId)
                .errorMessage(errorMessage)
                .createdAt(OffsetDateTime.now())
                .build();
        notificationLogRepo.save(log);

        return status;
    }
}

