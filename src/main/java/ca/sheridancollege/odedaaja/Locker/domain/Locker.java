package ca.sheridancollege.odedaaja.Locker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
    name = "locker", // 👈 make name explicit
    uniqueConstraints = @UniqueConstraint(columnNames = {"location_id", "lockerNumber"})
)
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Explicitly define join column and reference
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id", nullable = false)
    private LockerLocation location;

    @Column(nullable = false)
    private String lockerNumber;

    private boolean active = true;
    private boolean online = true;

    @Column(nullable = false)
    private BigDecimal basePrice;

    private String description;
}
