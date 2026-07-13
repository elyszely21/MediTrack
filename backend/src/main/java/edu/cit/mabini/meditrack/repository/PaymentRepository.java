package edu.cit.mabini.meditrack.billing;

import edu.cit.mabini.meditrack.billing.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillIdOrderByPaymentDateAsc(Long billId);
}