package com.tronno.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tronno.app.models.PaymentAllocation;

/**
 * PaymentAllocationRepository
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
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {
    List<PaymentAllocation> findByPaymentIdOrderByIdAsc(Long paymentId);
    List<PaymentAllocation> findByScheduleId(Long scheduleId);
    void deleteByPaymentId(Long paymentId);
}
