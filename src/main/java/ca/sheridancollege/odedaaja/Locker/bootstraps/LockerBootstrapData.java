package ca.sheridancollege.odedaaja.Locker.bootstraps;

import ca.sheridancollege.odedaaja.Locker.domain.*;
import ca.sheridancollege.odedaaja.Locker.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LockerBootstrapData implements CommandLineRunner {

    private final LockerLocationRepository locationRepo;
    private final LockerRepository lockerRepo;
    private final PricingRuleRepository pricingRepo;

    @Override
    public void run(String... args) throws Exception {
        createPricingRules();
        createLocations();
        createLockers();
    }

    private void createPricingRules() {
        if (pricingRepo.count() == 0) {
            PricingRule pricingRule = PricingRule.builder()
                    .basePrice(new BigDecimal("50.00")) // Base price (quarterly price)
                    .taxPercent(new BigDecimal("13.0")) // 13% tax
                    .feeFlat(new BigDecimal("5.00")) // $5 processing fee
                    .quarterlyMultiplier(new BigDecimal("1.0")) // 1 quarter = base price
                    .twoQuartersMultiplier(new BigDecimal("2.0")) // 2 quarters = 2x base price
                    .yearlyMultiplier(new BigDecimal("3.0")) // 1 year = 3 quarters = 3x base price
                    .build();
            
            pricingRepo.save(pricingRule);
            System.out.println("✅ Created default pricing rule");
        }
    }

    private void createLocations() {
        if (locationRepo.count() == 0) {
            LockerLocation[] locations = {
                LockerLocation.builder().building("Building A").floor("1st Floor").build(),
                LockerLocation.builder().building("Building A").floor("2nd Floor").build(),
                LockerLocation.builder().building("Building B").floor("1st Floor").build(),
                LockerLocation.builder().building("Building B").floor("2nd Floor").build(),
                LockerLocation.builder().building("Building C").floor("1st Floor").build(),
                LockerLocation.builder().building("Building C").floor("2nd Floor").build(),
                LockerLocation.builder().building("Building D").floor("1st Floor").build(),
                LockerLocation.builder().building("Building D").floor("2nd Floor").build()
            };

            for (LockerLocation location : locations) {
                locationRepo.save(location);
            }
            System.out.println("✅ Created " + locations.length + " locker locations");
        }
    }

    private void createLockers() {
        if (lockerRepo.count() == 0) {
            var locations = locationRepo.findAll();
            int lockerNumber = 1;

            for (LockerLocation location : locations) {
                // Create 10 lockers per location
                for (int i = 0; i < 10; i++) {
                    Locker locker = Locker.builder()
                            .lockerNumber("L" + String.format("%03d", lockerNumber))
                            .location(location)
                            .basePrice(new BigDecimal("50.00")) // $50 per quarter (4 months)
                            .active(true)
                            .online(true)
                            .build();
                    
                    lockerRepo.save(locker);
                    lockerNumber++;
                }
            }
            System.out.println("✅ Created " + (lockerNumber - 1) + " lockers");
        }
    }
}
