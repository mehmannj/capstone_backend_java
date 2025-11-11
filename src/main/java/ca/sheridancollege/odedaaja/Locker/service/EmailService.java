package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.EmailStatus;

public interface EmailService {
    EmailStatus sendReceipt(String userEmail, Long bookingId, String receiptUrl);
    EmailStatus sendContract(String userEmail, Long bookingId, String contractUrl);
}