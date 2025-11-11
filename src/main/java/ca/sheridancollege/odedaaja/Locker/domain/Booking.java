package ca.sheridancollege.odedaaja.Locker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(indexes = {
        @Index(name="idx_booking_locker", columnList = "locker_id"),
        @Index(name="idx_booking_user", columnList = "userEmail")
})
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Locker locker;

    private String userEmail;
    private String userName;

    @Enumerated(EnumType.STRING)
    private DurationOption duration;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal priceSubTotal;
    private BigDecimal taxAmount;
    private BigDecimal feesAmount;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String paymentProvider;
    private String paymentReference;

    private String receiptUrl;
    private String contractUrl;

    // Cancellation fields
    private String cancellationReason;
    private OffsetDateTime cancellationRequestDate;
    private OffsetDateTime cancellationProcessedDate;
    private String cancellationProcessedBy;
    private BigDecimal refundAmount;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
