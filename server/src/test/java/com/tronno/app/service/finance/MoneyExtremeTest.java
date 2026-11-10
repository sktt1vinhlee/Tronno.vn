package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MoneyExtremeTest
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
@DisplayName("Money.max()/min() - Chọn giá trị lớn nhỏ")
class MoneyExtremeTest {

    @Test
    @DisplayName("max() trả về giá trị lớn hơn")
    void maxReturnsLarger() {
        assertThat(Money.max(new BigDecimal("10"), new BigDecimal("20")))
                .isEqualByComparingTo("20");
        assertThat(Money.max(new BigDecimal("10"), null)).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("min() trả về giá trị nhỏ hơn")
    void minReturnsSmaller() {
        assertThat(Money.min(new BigDecimal("10"), new BigDecimal("20")))
                .isEqualByComparingTo("10");
        assertThat(Money.min(null, new BigDecimal("20"))).isEqualByComparingTo("0");
    }
}
