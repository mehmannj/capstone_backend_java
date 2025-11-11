package ca.sheridancollege.odedaaja.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String category; // LAPTOP, BIKE, PROJECTOR, CAMERA, etc.
    private String location; // Where to pick up the resource
    private String condition; // NEW, GOOD, FAIR, POOR
    private String specifications; // Technical details
    
    private boolean available = true;
    private boolean active = true;
    
    private BigDecimal rentalPricePerDay;
    private BigDecimal depositAmount;
    
    private String imageUrl; // For displaying resource images
    
    @ManyToOne
    private Department department; // Which department manages this resource
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
