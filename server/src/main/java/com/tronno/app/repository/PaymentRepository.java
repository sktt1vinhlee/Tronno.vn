package com.tronno.app.repository;

import com.tronno.app.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByDebtIdOrderByPaidAtDescIdDesc(Long debtId);
    List<Payment> findByUserIdOrderByPaidAtDescIdDesc(Long userId);
    List<Payment> findByUserIdAndPaidAtBetweenOrderByPaidAtAsc(Long userId, LocalDate from, LocalDate to);
    List<Payment> findByDebtIdAndStatusOrderByPaidAtAscIdAsc(Long debtId, String status);
}
