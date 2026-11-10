package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tronno.app.models.Debt;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleType;
import com.tronno.app.models.TermType;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.debt;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.generate;

/**
 * ScheduleGeneratorQuarterlyTest
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
@DisplayName("ScheduleGenerator.generate() - Lịch trả theo quý")
public class ScheduleGeneratorQuarterlyTest {

    @Test
    @DisplayName("Kỳ hạn 12 tháng theo quý sinh 4 kỳ trả")
    void quarterlyCreatesFourInstallments() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        d.setTermType(TermType.QUARTERLY);

        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        assertThat(result).hasSize(4);
        // Kỳ 1 kết thúc 01/04/2026 là thứ Tư nên giữ nguyên.
        assertThat(result.get(0).getPeriodEnd()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(result.get(0).getDueDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        // Kỳ 2 kết thúc 01/07/2026.
        assertThat(result.get(1).getPeriodEnd()).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    @DisplayName("Tổng gốc vẫn bằng số tiền vay")
    void quarterlyTotalPrincipalIsCorrect() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        d.setTermType(TermType.QUARTERLY);

        List<RepaymentSchedule> result = generate(d, Money.zero(), Money.zero());

        BigDecimal totalPrincipal = result.stream()
                .map(RepaymentSchedule::getPrincipalDue)
                .reduce(BigDecimal.ZERO, Money::add);

        assertThat(totalPrincipal).isEqualByComparingTo("12000000.00");
    }
}
