package net.start.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.start.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
