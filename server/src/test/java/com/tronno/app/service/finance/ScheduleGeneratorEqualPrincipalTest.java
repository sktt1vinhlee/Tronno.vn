package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tronno.app.models.Debt;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleStatus;
import com.tronno.app.models.ScheduleType;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.DISBURSE;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.debt;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.generate;

/**
 * ScheduleGeneratorEqualPrincipalTest
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
@DisplayName("ScheduleGenerator.generate() - Lịch trả gốc đều")
public class ScheduleGeneratorEqualPrincipalTest {

    @Test
    @DisplayName("Sinh đúng số kỳ trả theo kỳ hạn")
    void generatesExpectedInstallments() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        assertThat(result).hasSize(12);
        assertThat(result.get(0).getInstallmentNo()).isEqualTo(1);
        assertThat(result.get(11).getInstallmentNo()).isEqualTo(12);
    }

    @Test
    @DisplayName("Tổng gốc đủ hạn bằng số tiền vay")
    void totalPrincipalEqualsDebtPrincipal() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        BigDecimal totalPrincipal = result.stream()
                .map(RepaymentSchedule::getPrincipalDue)
                .reduce(BigDecimal.ZERO, Money::add);

        assertThat(totalPrincipal).isEqualByComparingTo("12000000.00");
    }

    @Test
    @DisplayName("Ngày đến hạn kỳ đầu là sau 1 tháng, dời sang ngày làm việc nếu rơi cuối tuần")
    void firstDueDateIsOneMonthLater() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        // Kỳ hạn 01/02/2026 là Chủ nhật nên được dời sang thứ Hai 02/02/2026.
        assertThat(result.get(0).getPeriodStart()).isEqualTo(DISBURSE);
        assertThat(result.get(0).getPeriodEnd()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(result.get(0).getDueDate()).isEqualTo(LocalDate.of(2026, 2, 2));
    }

    @Test
    @DisplayName("Ngày đến hạn rơi vào ngày làm việc thì giữ nguyên")
    void dueDateKeepsBusinessDay() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        // Kỳ thứ 3 kết thúc 01/04/2026 là thứ Tư, giữ nguyên.
        assertThat(result.get(2).getDueDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("Lãi suất 0 thì không phát sinh tiền lãi")
    void zeroRateProducesNoInterest() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        assertThat(result).allSatisfy(s -> assertThat(s.getInterestDue())
                .isEqualByComparingTo("0.00"));
    }

    @Test
    @DisplayName("Tất cả kỳ trả ở trạng thái PENDING khi chưa trả")
    void allSchedulesArePending() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        assertThat(result).allSatisfy(s ->
                assertThat(s.getStatus()).isEqualTo(ScheduleStatus.PENDING));
    }
}
