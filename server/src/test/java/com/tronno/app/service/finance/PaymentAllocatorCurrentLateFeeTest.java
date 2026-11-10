package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tronno.app.models.Debt;
import com.tronno.app.models.LateFeeType;
import com.tronno.app.models.RepaymentSchedule;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tronno.app.service.finance.PaymentAllocatorTestData.TODAY;
import static com.tronno.app.service.finance.PaymentAllocatorTestData.debt;
import static com.tronno.app.service.finance.PaymentAllocatorTestData.schedule;

/**
 * PaymentAllocatorCurrentLateFeeTest
 *
 * Version 1.0
 *
 * Date: 10-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 10-11-2026         VinhHL             Create
 */
@DisplayName("PaymentAllocator.currentLateFee() - Phí trễ hạn hiện tại")
class PaymentAllocatorCurrentLateFeeTest {

    @Test
    @DisplayName("Chưa tới hạn thì không có phí")
    void noFeeBeforeDueDate() {
        Debt d = debt();
        d.setLateFeeType(LateFeeType.FIXED_PER_DAY);
        d.setLateFeeAmount(new BigDecimal("1000"));

        RepaymentSchedule s = schedule(1L, 1, TODAY.plusDays(5), "1000000.00", "0.00", "0.00");

        assertThat(PaymentAllocator.currentLateFee(d, s, TODAY)).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Trễ hạn thì tính phí theo số ngày")
    void feeIsCalculatedWhenOverdue() {
        Debt d = debt();
        d.setLateFeeType(LateFeeType.FIXED_PER_DAY);
        d.setLateFeeAmount(new BigDecimal("1000"));

        RepaymentSchedule s = schedule(1L, 1, TODAY.minusDays(4), "1000000.00", "0.00", "0.00");

        assertThat(PaymentAllocator.currentLateFee(d, s, TODAY)).isEqualByComparingTo("4000.00");
    }

    @Test
    @DisplayName("Khoản nợ null thì trả về 0")
    void nullDebtReturnsZero() {
        RepaymentSchedule s = schedule(1L, 1, TODAY.minusDays(4), "1000000.00", "0.00", "0.00");
        assertThat(PaymentAllocator.currentLateFee(null, s, TODAY)).isEqualByComparingTo("0.00");
        assertThat(PaymentAllocator.currentLateFee(debt(), null, TODAY))
                .isEqualByComparingTo("0.00");
    }
}
