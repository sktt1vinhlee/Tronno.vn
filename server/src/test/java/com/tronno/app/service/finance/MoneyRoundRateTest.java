package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MoneyRoundRateTest
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
@DisplayName("Money.roundRate() - Làm tròn lãi suất 8 chữ số thập phân")
public class MoneyRoundRateTest {

    @Test
    @DisplayName("Giữ tối đa 8 chữ số thập phân")
    void keepsEightDecimals() {
        assertThat(Money.roundRate(new BigDecimal("0.123456789")))
                .isEqualByComparingTo("0.12345679");
        assertThat(Money.roundRate(new BigDecimal("0.1")).scale())
                .isEqualTo(Money.RATE_SCALE);
    }

    @Test
    @DisplayName("null trả về 0 với 8 chữ số thập phân")
    void nullReturnsZero() {
        assertThat(Money.roundRate(null)).isEqualByComparingTo("0");
        assertThat(Money.roundRate(null).scale()).isEqualTo(Money.RATE_SCALE);
    }
}
