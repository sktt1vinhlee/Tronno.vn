package com.tronno.app.service.finance;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DateRulesBoundariesTest
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
@DisplayName("DateRules.endOfMonth()/startOfMonth()/lastDayOfMonth()")
public class DateRulesBoundariesTest {

    @Test
    @DisplayName("endOfMonth() trả về ngày cuối tháng")
    void endOfMonthWorks() {
        assertThat(DateRules.endOfMonth(LocalDate.of(2026, 2, 10)))
                .isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(DateRules.endOfMonth(LocalDate.of(2024, 2, 10)))
                .isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    @DisplayName("startOfMonth() trả về ngày 1")
    void startOfMonthWorks() {
        assertThat(DateRules.startOfMonth(LocalDate.of(2026, 2, 28)))
                .isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    @DisplayName("lastDayOfMonth() trùng endOfMonth()")
    void lastDayOfMonthWorks() {
        LocalDate date = LocalDate.of(2026, 7, 9);
        assertThat(DateRules.lastDayOfMonth(date)).isEqualTo(DateRules.endOfMonth(date));
    }
}
