package ca.sheridancollege.odedaaja.Locker.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.sheridancollege.odedaaja.Locker.domain.ContractTemplate;

public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, Long> {
    Optional<ContractTemplate> findFirstByActiveTrueOrderByVersionDesc();
}
