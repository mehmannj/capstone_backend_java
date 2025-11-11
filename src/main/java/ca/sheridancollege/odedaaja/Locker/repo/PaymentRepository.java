package ca.sheridancollege.odedaaja.Locker.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.sheridancollege.odedaaja.Locker.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
