package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tronno.app.models.InterestRateHistory;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tronno.app.service.finance.ScheduleGeneratorTestData.DISBURSE;

/**
 * ScheduleGeneratorRateForTest
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
@DisplayName("ScheduleGenerator.rateFor() - Tra cứu lãi suất theo ngày")
class ScheduleGeneratorRateForTest {

    @Test
    @DisplayName("Không có lịch sử thì dùng lãi suất mặc định")
    void usesFallbackWhenNoHistory() {
        BigDecimal fallback = new BigDecimal("10");
        assertThat(ScheduleGenerator.rateFor(List.of(), DISBURSE, fallback))
                .isEqualByComparingTo("10");
        assertThat(ScheduleGenerator.rateFor(null, DISBURSE, fallback))
                .isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("Lấy lãi suất có hiệu lực tại ngày cần tra cứu")
    void picksEffectiveRate() {
        InterestRateHistory h = InterestRateHistory.builder()
                .debtId(1L)
                .annualRate(new BigDecimal("12"))
                .effectiveFrom(LocalDate.of(2026, 6, 1))
                .build();

        assertThat(ScheduleGenerator.rateFor(List.of(h), LocalDate.of(2026, 7, 1),
                new BigDecimal("10"))).isEqualByComparingTo("12");
    }

    @Test
    @DisplayName("Lịch sử chưa tới hạn hiệu lực không được dùng")
    void ignoresNotYetEffectiveHistory() {
        InterestRateHistory h = InterestRateHistory.builder()
                .debtId(1L)
                .annualRate(new BigDecimal("12"))
                .effectiveFrom(LocalDate.of(2026, 6, 1))
                .build();

        assertThat(ScheduleGenerator.rateFor(List.of(h), LocalDate.of(2026, 3, 1),
                new BigDecimal("10"))).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("Lịch sử đã hết hiệu lực không được dùng")
    void ignoresExpiredHistory() {
        InterestRateHistory h = InterestRateHistory.builder()
                .debtId(1L)
                .annualRate(new BigDecimal("12"))
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 5, 31))
                .build();

        assertThat(ScheduleGenerator.rateFor(List.of(h), LocalDate.of(2026, 7, 1),
                new BigDecimal("10"))).isEqualByComparingTo("10");
    }
}
