package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;

public interface ReceiptService {
    String generateReceipt(Booking booking);
    String generateReceiptContent(Booking booking);
}