package ca.sheridancollege.odedaaja.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Resource resource;

    @ManyToOne
    private Users student; // Student who is renting

    private String studentEmail;
    private String studentPhone;
    private String studentName;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private LocalDateTime actualReturnTime;

    @Enumerated(EnumType.STRING)
    private RentalStatus status = RentalStatus.PENDING;

    private int rentalDays;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal refundAmount;

    private String pickupLocation;
    private String returnLocation;
    private String notes; // Additional notes from student
    private String adminNotes; // Notes from coordinator

    @ManyToOne
    private Users approvedBy; // Coordinator who approved/denied

    public enum RentalStatus {
        PENDING,        // Student requested, waiting for approval
        APPROVED,       // Coordinator approved, ready for pickup
        PICKED_UP,      // Student picked up the resource
        RETURNED,       // Student returned the resource
        DENIED,         // Coordinator denied the request
        CANCELLED,      // Student cancelled the request
        OVERDUE         // Resource not returned on time
    }
}
