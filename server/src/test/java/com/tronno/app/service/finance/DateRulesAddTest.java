package com.tronno.app.service.finance;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DateRulesAddTest
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
@DisplayName("DateRules.addMonths()/addQuarters()")
public class DateRulesAddTest {

    @Test
    @DisplayName("addMonths() cộng số tháng")
    void addMonthsWorks() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        assertThat(DateRules.addMonths(start, 3)).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(DateRules.addMonths(start, 12)).isEqualTo(LocalDate.of(2027, 1, 15));
    }

    @Test
    @DisplayName("addMonths() với số tháng âm lùi về trước")
    void addNegativeMonthsGoesBack() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        assertThat(DateRules.addMonths(start, -1)).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    @DisplayName("addQuarters() cộng 3 tháng mỗi quý")
    void addQuartersWorks() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        assertThat(DateRules.addQuarters(start, 1)).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(DateRules.addQuarters(start, 4)).isEqualTo(LocalDate.of(2027, 1, 15));
    }
}
