package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.*;
import ca.sheridancollege.odedaaja.Locker.repo.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final NotificationLogRepository logRepo;

    @Override
    public EmailStatus sendReceipt(String to, Long bookingId, String receiptUrl) {
        return send(to, bookingId, EmailType.RECEIPT,
                "Your Locker Receipt", "Download: " + receiptUrl);
    }

    @Override
    public EmailStatus sendContract(String to, Long bookingId, String contractUrl) {
        return send(to, bookingId, EmailType.CONTRACT,
                "Your Locker Contract", "Download: " + contractUrl);
    }

    private EmailStatus send(String to, Long bookingId, EmailType type,
                             String subject, String body) {
        EmailStatus status = EmailStatus.SENT;
        String providerId = UUID.randomUUID().toString();
        String err = null;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception ex) {
            status = EmailStatus.FAILED;
            err = ex.getMessage();
        }

        logRepo.save(NotificationLog.builder()
                .bookingId(bookingId)
                .type(type)
                .status(status)
                .providerMessageId(providerId)
                .errorMessage(err)
                .createdAt(OffsetDateTime.now())
                .build());

        return status;
    }
}
