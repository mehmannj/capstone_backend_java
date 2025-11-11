package ca.sheridancollege.odedaaja.Locker.service;

import java.math.BigDecimal;

public interface PaymentService {
    String createPaymentIntent(BigDecimal amount, String currency, String customerEmail);
    void markPaid(Long bookingId, String providerPaymentId);
    void markFailed(Long bookingId, String errorMessage);
}