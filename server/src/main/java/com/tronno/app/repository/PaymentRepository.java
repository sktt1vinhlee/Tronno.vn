package com.tronno.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tronno.app.models.Payment;

/**
 * PaymentRepository
 *
 * Version 1.0
 *
 * Date: 09-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 09-11-2026         VinhHL             Create
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByDebtIdOrderByPaidAtDescIdDesc(Long debtId);
    List<Payment> findByUserIdOrderByPaidAtDescIdDesc(Long userId);
    List<Payment> findByUserIdAndPaidAtBetweenOrderByPaidAtAsc(Long userId, LocalDate from, LocalDate to);
    List<Payment> findByDebtIdAndStatusOrderByPaidAtAscIdAsc(Long debtId, String status);
}
