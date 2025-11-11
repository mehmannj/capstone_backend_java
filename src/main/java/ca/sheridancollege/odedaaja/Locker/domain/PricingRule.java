package ca.sheridancollege.odedaaja.Locker.domain;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class PricingRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal basePrice;
    private BigDecimal taxPercent;
    private BigDecimal feeFlat;

    private BigDecimal quarterlyMultiplier;
    private BigDecimal twoQuartersMultiplier;
    private BigDecimal yearlyMultiplier;
}
