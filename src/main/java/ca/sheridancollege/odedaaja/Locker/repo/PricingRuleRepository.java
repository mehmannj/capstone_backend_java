package ca.sheridancollege.odedaaja.Locker.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.sheridancollege.odedaaja.Locker.domain.PricingRule;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
}
