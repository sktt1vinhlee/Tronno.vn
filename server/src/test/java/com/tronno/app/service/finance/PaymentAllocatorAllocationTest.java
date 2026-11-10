package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.util.List;

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
 * PaymentAllocatorAllocationTest
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
@DisplayName("PaymentAllocator.allocate() - Thứ tự ưu tiên phân bổ")
public class PaymentAllocatorAllocationTest {

    @Test
    @DisplayName("Trả tiền được trừ lãi trước rồi tới gốc")
    void appliesInterestBeforePrincipal() {
        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY, "1000000.00", "50000.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("50000"), schedules, debt(), TODAY, null);

        assertThat(result.applied()).isEqualByComparingTo("50000.00");
        assertThat(result.surplus()).isEqualByComparingTo("0.00");
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().get(0).component())
                .isEqualTo(PaymentAllocator.COMPONENT_INTEREST);
        assertThat(result.allocations().get(0).amount()).isEqualByComparingTo("50000.00");
    }

    @Test
    @DisplayName("Trả vượt lãi sẽ dồn sang gốc")
    void overflowGoesToPrincipal() {
        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY, "1000000.00", "50000.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("200000"), schedules, debt(), TODAY, null);

        assertThat(result.allocations()).hasSize(2);
        assertThat(result.allocations().get(0).component())
                .isEqualTo(PaymentAllocator.COMPONENT_INTEREST);
        assertThat(result.allocations().get(1).component())
                .isEqualTo(PaymentAllocator.COMPONENT_PRINCIPAL);
        assertThat(result.allocations().get(1).amount()).isEqualByComparingTo("150000.00");
        assertThat(result.applied()).isEqualByComparingTo("200000.00");
    }

    @Test
    @DisplayName("Phí phạt trễ hạn được ưu tiên cao nhất")
    void lateFeeTakesPriority() {
        Debt d = debt();
        d.setLateFeeType(LateFeeType.FIXED_PER_DAY);
        d.setLateFeeAmount(new BigDecimal("1000"));

        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY.minusDays(10), "1000000.00", "50000.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("5000"), schedules, d, TODAY, null);

        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().get(0).component())
                .isEqualTo(PaymentAllocator.COMPONENT_LATE_FEE);
        assertThat(result.allocations().get(0).amount()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("Phí trễ hạn chỉ tính sau khi trừ ngày ân hạn")
    void graceDaysReduceLateFee() {
        Debt d = debt();
        d.setLateFeeType(LateFeeType.FIXED_PER_DAY);
        d.setLateFeeAmount(new BigDecimal("1000"));
        d.setGraceDays(5);

        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY.minusDays(8), "1000000.00", "0.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("3000"), schedules, d, TODAY, null);

        // Trễ 8 ngày - 5 ngày ân hạn = 3 ngày tính phí
        assertThat(result.allocations().get(0).amount()).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("Tiền trả nhiều hơn tổng nợ thì phần dư được trả về")
    void surplusIsReturned() {
        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY, "1000000.00", "0.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("1500000"), schedules, debt(), TODAY, null);

        assertThat(result.applied()).isEqualByComparingTo("1000000.00");
        assertThat(result.surplus()).isEqualByComparingTo("500000.00");
    }

    @Test
    @DisplayName("Trả bằng 0 thì không có khoản phân bổ nào")
    void zeroAmountProducesNoAllocation() {
        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY, "1000000.00", "0.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                BigDecimal.ZERO, schedules, debt(), TODAY, null);

        assertThat(result.allocations()).isEmpty();
        assertThat(result.applied()).isEqualByComparingTo("0.00");
        assertThat(result.surplus()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Chỉ phân bổ vào kỳ trả được chỉ định")
    void respectsTargetSchedule() {
        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY, "1000000.00", "0.00", "0.00"),
                schedule(2L, 2, TODAY.plusMonths(1), "1000000.00", "0.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("100000"), schedules, debt(), TODAY, 2L);

        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().get(0).scheduleId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Bỏ qua kỳ trả quá xa trong tương lai")
    void skipsFarFutureSchedule() {
        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY.plusDays(60), "1000000.00", "0.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("100000"), schedules, debt(), TODAY, null);

        assertThat(result.allocations()).isEmpty();
        assertThat(result.surplus()).isEqualByComparingTo("100000.00");
    }

    @Test
    @DisplayName("Phân bổ lần lượt qua nhiều kỳ trả")
    void spreadsAcrossMultipleSchedules() {
        List<RepaymentSchedule> schedules = List.of(
                schedule(1L, 1, TODAY, "500000.00", "0.00", "0.00"),
                schedule(2L, 2, TODAY, "500000.00", "0.00", "0.00"));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                new BigDecimal("1200000"), schedules, debt(), TODAY, null);

        assertThat(result.allocations()).hasSize(2);
        assertThat(result.applied()).isEqualByComparingTo("1000000.00");
        assertThat(result.surplus()).isEqualByComparingTo("200000.00");
    }
}
