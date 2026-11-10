package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tronno.app.models.Debt;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleType;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.debt;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.generate;

/**
 * ScheduleGeneratorAnnuityTest
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
@DisplayName("ScheduleGenerator.generate() - Lịch trả niên kim")
class ScheduleGeneratorAnnuityTest {

    @Test
    @DisplayName("Tiền trả mỗi kỳ bằng nhau, kỳ cuối giữ phần gốc còn lại")
    void installmentAmountsAreEqualExceptLast() {
        Debt d = debt(ScheduleType.ANNUITY, InterestMethod.REDUCING, 12);
        d.setAnnualRate(new BigDecimal("12"));

        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        assertThat(result).hasSize(12);

        BigDecimal first = result.get(0).getTotalDue();
        for (int i = 0; i < 11; i++) {
            assertThat(result.get(i).getTotalDue()).isEqualByComparingTo(first);
        }

        // Kỳ cuối gán đúng phần gốc còn lại nên tổng có thể lệch nhỏ.
        assertThat(result.get(11).getTotalDue()).isGreaterThan(first);
    }

    @Test
    @DisplayName("Tổng tiền trả bằng gốc cộng lãi")
    void totalPaidEqualsPrincipalPlusInterest() {
        Debt d = debt(ScheduleType.ANNUITY, InterestMethod.REDUCING, 12);
        d.setAnnualRate(new BigDecimal("12"));

        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        BigDecimal total = result.stream()
                .map(RepaymentSchedule::getTotalDue)
                .reduce(BigDecimal.ZERO, Money::add);
        BigDecimal interest = result.stream()
                .map(RepaymentSchedule::getInterestDue)
                .reduce(BigDecimal.ZERO, Money::add);

        assertThat(total).isEqualByComparingTo(Money.add(new BigDecimal("12000000"), interest));
    }

    @Test
    @DisplayName("Tổng gốc trong lịch niên kim bằng số tiền vay")
    void annuityTotalPrincipalIsCorrect() {
        Debt d = debt(ScheduleType.ANNUITY, InterestMethod.REDUCING, 12);
        d.setAnnualRate(new BigDecimal("12"));

        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        BigDecimal totalPrincipal = result.stream()
                .map(RepaymentSchedule::getPrincipalDue)
                .reduce(BigDecimal.ZERO, Money::add);

        assertThat(totalPrincipal).isEqualByComparingTo("12000000.00");
    }
}
