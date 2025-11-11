package ca.sheridancollege.odedaaja.Locker.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CheckoutSummary(
    Long bookingId,
    String building,
    String floor,
    String lockerNumber,
    String durationLabel,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal subTotal,
    BigDecimal tax,
    BigDecimal fees,
    BigDecimal total,
    String paymentIntentId,
    String status
) {}
