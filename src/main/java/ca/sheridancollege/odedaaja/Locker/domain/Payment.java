package ca.sheridancollege.odedaaja.Locker.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private String provider;
    private String providerPaymentId;
    private String status;
    private BigDecimal amount;
    private OffsetDateTime createdAt;
}
