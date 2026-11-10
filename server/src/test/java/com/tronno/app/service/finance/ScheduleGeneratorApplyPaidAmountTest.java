package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tronno.app.models.Debt;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleStatus;
import com.tronno.app.models.ScheduleType;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.debt;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.generate;

/**
 * ScheduleGeneratorApplyPaidAmountTest
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
@DisplayName("ScheduleGenerator.generate() - Áp dụng tiền đã trả")
class ScheduleGeneratorApplyPaidAmountTest {

    @Test
    @DisplayName("Tiền đã trả được phân bổ vào kỳ trả đầu tiên")
    void paidAmountIsAppliedToFirstSchedule() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), new BigDecimal("500000"));

        assertThat(result.get(0).getPaidAmount()).isEqualByComparingTo("500000.00");
        assertThat(result.get(0).getStatus()).isEqualTo(ScheduleStatus.PARTIAL);
        assertThat(result.get(1).getPaidAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Trả đủ một kỳ thì kỳ đó chuyển sang PAID")
    void fullyPaidScheduleBecomesPaid() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), new BigDecimal("1000000"));

        assertThat(result.get(0).getStatus()).isEqualTo(ScheduleStatus.PAID);
        assertThat(result.get(0).getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("Gốc đã trả được trừ khỏi dư nợ sinh lịch mới")
    void paidPrincipalReducesOutstanding() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(
                d, new BigDecimal("2000000"), new BigDecimal("2000000"));

        BigDecimal totalPrincipal = result.stream()
                .map(RepaymentSchedule::getPrincipalDue)
                .reduce(BigDecimal.ZERO, Money::add);

        assertThat(totalPrincipal).isEqualByComparingTo("10000000.00");
    }
}
