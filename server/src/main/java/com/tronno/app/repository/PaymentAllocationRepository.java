package com.tronno.app.repository;

import com.tronno.app.models.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {
    List<PaymentAllocation> findByPaymentIdOrderByIdAsc(Long paymentId);
    List<PaymentAllocation> findByScheduleId(Long scheduleId);
    void deleteByPaymentId(Long paymentId);
}
