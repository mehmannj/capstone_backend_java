package ca.sheridancollege.odedaaja.Locker.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ca.sheridancollege.odedaaja.Locker.domain.DurationOption;
import ca.sheridancollege.odedaaja.Locker.domain.PricingRule;
import ca.sheridancollege.odedaaja.Locker.repo.PricingRuleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PricingService {
    private final PricingRuleRepository pricingRepo;

    public record PriceBreakdown(BigDecimal subTotal, BigDecimal tax, BigDecimal fees, BigDecimal total) {}

    public PriceBreakdown calculate(DurationOption duration) {
        Optional<PricingRule> opt = pricingRepo.findAll().stream().findFirst();

        if (opt.isEmpty()) {
            // fallback defaults
            BigDecimal base = new BigDecimal("50.00"); // Base price (quarterly price)
            BigDecimal taxPct = new BigDecimal("13.0");
            BigDecimal fee = new BigDecimal("5.00");

            BigDecimal mult = switch (duration) {
                case QUARTERLY -> new BigDecimal("1.0"); // 1 quarter = base price
                case TWO_QUARTERS -> new BigDecimal("2.0"); // 2 quarters = 2x base price
                case YEARLY -> new BigDecimal("3.0"); // 1 year = 3 quarters = 3x base price
            };

            BigDecimal sub = base.multiply(mult);
            BigDecimal tax = sub.multiply(taxPct).divide(new BigDecimal("100"));
            BigDecimal total = sub.add(tax).add(fee);
            return new PriceBreakdown(sub, tax, fee, total);
        }

        PricingRule r = opt.get();
        BigDecimal mult = switch (duration) {
            case QUARTERLY -> r.getQuarterlyMultiplier();
            case TWO_QUARTERS -> r.getTwoQuartersMultiplier();
            case YEARLY -> r.getYearlyMultiplier();
        };

        BigDecimal sub = r.getBasePrice().multiply(mult);
        BigDecimal tax = sub.multiply(r.getTaxPercent()).divide(new BigDecimal("100"));
        BigDecimal total = sub.add(tax).add(r.getFeeFlat());
        return new PriceBreakdown(sub, tax, r.getFeeFlat(), total);
    }
}
