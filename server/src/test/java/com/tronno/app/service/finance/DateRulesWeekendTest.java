package com.tronno.app.service.finance;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DateRulesWeekendTest
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
@DisplayName("DateRules.isWeekend()/adjustToBusinessDay()")
class DateRulesWeekendTest {

    @Test
    @DisplayName("isWeekend() đúng với T7 và CN")
    void isWeekendWorks() {
        assertThat(DateRules.isWeekend(LocalDate.of(2026, 11, 7))).isTrue();
        assertThat(DateRules.isWeekend(LocalDate.of(2026, 11, 8))).isTrue();
        assertThat(DateRules.isWeekend(LocalDate.of(2026, 11, 9))).isFalse();
    }

    @Test
    @DisplayName("adjustToBusinessDay() dời T7 sang T2 tuần sau")
    void adjustSaturdayToMonday() {
        assertThat(DateRules.adjustToBusinessDay(LocalDate.of(2026, 11, 7)))
                .isEqualTo(LocalDate.of(2026, 11, 9));
    }

    @Test
    @DisplayName("adjustToBusinessDay() dời CN sang T2 tuần sau")
    void adjustSundayToMonday() {
        assertThat(DateRules.adjustToBusinessDay(LocalDate.of(2026, 11, 8)))
                .isEqualTo(LocalDate.of(2026, 11, 9));
    }

    @Test
    @DisplayName("adjustToBusinessDay() giữ nguyên ngày làm việc")
    void keepsBusinessDay() {
        LocalDate monday = LocalDate.of(2026, 11, 9);
        assertThat(DateRules.adjustToBusinessDay(monday)).isEqualTo(monday);
    }
}
