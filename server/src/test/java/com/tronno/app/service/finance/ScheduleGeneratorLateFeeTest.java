package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tronno.app.models.Debt;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.LateFeeType;
import com.tronno.app.models.ScheduleType;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.debt;

/**
 * ScheduleGeneratorLateFeeTest
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
@DisplayName("ScheduleGenerator.lateFee() - Phí phạt trễ hạn")
class ScheduleGeneratorLateFeeTest {

    @Test
    @DisplayName("Chưa trễ hạn thì không có phí")
    void noFeeWhenNotOverdue() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        assertThat(ScheduleGenerator.lateFee(d, 0, new BigDecimal("1000000")))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Phí theo tỷ lệ % mỗi ngày")
    void percentPerDayFee() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        d.setLateFeeType(LateFeeType.PERCENT_PER_DAY);
        d.setLateFeeRate(new BigDecimal("0.05"));

        // 1.000.000 * 0.05% * 10 ngày = 5.000
        assertThat(ScheduleGenerator.lateFee(d, 10, new BigDecimal("1000000")))
                .isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("Phí cố định mỗi ngày")
    void fixedPerDayFee() {
        Debt d = debt(ScheduleType.EQUAL_PRINCIPAL, InterestMethod.REDUCING, 12);
        d.setLateFeeType(LateFeeType.FIXED_PER_DAY);
        d.setLateFeeAmount(new BigDecimal("2000"));

        assertThat(ScheduleGenerator.lateFee(d, 3, new BigDecimal("1000000")))
                .isEqualByComparingTo("6000.00");
    }
}
