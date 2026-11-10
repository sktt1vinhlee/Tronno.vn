package com.tronno.app.service.finance;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DateRulesDaysTest
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
@DisplayName("DateRules.daysInclusive()/daysBetween()")
class DateRulesDaysTest {

    @Test
    @DisplayName("daysInclusive() tính cả hai đầu mốc")
    void daysInclusiveCountsBothEnds() {
        assertThat(DateRules.daysInclusive(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1))).isEqualTo(1);
        assertThat(DateRules.daysInclusive(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))).isEqualTo(31);
    }

    @Test
    @DisplayName("daysInclusive() trả về 0 khi ngày kết thúc trước ngày bắt đầu")
    void daysInclusiveReturnsZeroWhenReversed() {
        assertThat(DateRules.daysInclusive(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 1))).isZero();
    }

    @Test
    @DisplayName("daysBetween() không tính ngày kết thúc")
    void daysBetweenExcludesEnd() {
        assertThat(DateRules.daysBetween(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))).isEqualTo(30);
        assertThat(DateRules.daysBetween(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1))).isZero();
    }
}
