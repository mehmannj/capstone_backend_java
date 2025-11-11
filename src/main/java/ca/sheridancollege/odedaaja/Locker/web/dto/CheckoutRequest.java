package ca.sheridancollege.odedaaja.Locker.web.dto;

import ca.sheridancollege.odedaaja.Locker.domain.DurationOption;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CheckoutRequest(
    @NotNull Long lockerId,
    @NotNull DurationOption duration,
    @NotNull LocalDate startDate,
    @Email String userEmail,
    @NotBlank String userName
) {}
