package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MoneyRoundTest
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
@DisplayName("Money.round() - Làm tròn số tiền")
public class MoneyRoundTest {

    @Test
    @DisplayName("Làm tròn HALF_UP")
    void roundsHalfUp() {
        assertThat(Money.round(new BigDecimal("10.005"))).isEqualByComparingTo("10.01");
        assertThat(Money.round(new BigDecimal("10.004"))).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("null trả về 0.00")
    void nullReturnsZero() {
        assertThat(Money.round(null)).isEqualByComparingTo("0.00");
    }
}
