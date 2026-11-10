package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MoneyComparisonTest
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
@DisplayName("Money - Các phép so sánh")
public class MoneyComparisonTest {

    @Test
    @DisplayName("isZero() đúng khi bằng 0")
    void isZeroWorks() {
        assertThat(Money.isZero(BigDecimal.ZERO)).isTrue();
        assertThat(Money.isZero(null)).isTrue();
        assertThat(Money.isZero(new BigDecimal("0.01"))).isFalse();
    }

    @Test
    @DisplayName("isPositive() đúng khi lớn hơn 0")
    void isPositiveWorks() {
        assertThat(Money.isPositive(new BigDecimal("0.01"))).isTrue();
        assertThat(Money.isPositive(BigDecimal.ZERO)).isFalse();
        assertThat(Money.isPositive(null)).isFalse();
    }

    @Test
    @DisplayName("isNegative() đúng khi nhỏ hơn 0")
    void isNegativeWorks() {
        assertThat(Money.isNegative(new BigDecimal("-0.01"))).isTrue();
        assertThat(Money.isNegative(BigDecimal.ZERO)).isFalse();
        assertThat(Money.isNegative(null)).isFalse();
    }

    @Test
    @DisplayName("compareTo() trả về dấu của hiệu")
    void compareToWorks() {
        assertThat(Money.compareTo(new BigDecimal("10"), new BigDecimal("20"))).isNegative();
        assertThat(Money.compareTo(new BigDecimal("20"), new BigDecimal("10"))).isPositive();
        assertThat(Money.compareTo(new BigDecimal("10"), new BigDecimal("10"))).isZero();
    }
}
