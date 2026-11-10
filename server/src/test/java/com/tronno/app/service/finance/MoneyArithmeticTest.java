package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MoneyArithmeticTest
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
@DisplayName("Money - Phép tính cộng/trừ/nhân/chia")
public class MoneyArithmeticTest {

    @Test
    @DisplayName("add() cộng và làm tròn kết quả")
    void addWorks() {
        assertThat(Money.add(new BigDecimal("100.005"), new BigDecimal("0.005")))
                .isEqualByComparingTo("100.01");
    }

    @Test
    @DisplayName("subtract() trừ và làm tròn kết quả")
    void subtractWorks() {
        assertThat(Money.subtract(new BigDecimal("100.00"), new BigDecimal("0.005")))
                .isEqualByComparingTo("100.00");
        assertThat(Money.subtract(new BigDecimal("100.00"), new BigDecimal("50.00")))
                .isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("multiply() nhân và làm tròn kết quả")
    void multiplyWorks() {
        assertThat(Money.multiply(new BigDecimal("3.333"), new BigDecimal("3")))
                .isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("divide() chia với mẫu số khác 0")
    void divideWorks() {
        assertThat(Money.divide(new BigDecimal("100"), new BigDecimal("3")))
                .isEqualByComparingTo("33.33");
    }

    @Test
    @DisplayName("divide() với mẫu số bằng 0 trả về 0.00 thay vì ném lỗi")
    void divideByZeroReturnsZero() {
        assertThat(Money.divide(new BigDecimal("100"), BigDecimal.ZERO))
                .isEqualByComparingTo("0.00");
        assertThat(Money.divide(new BigDecimal("100"), null))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Phép tính với null được coi như 0")
    void nullIsTreatedAsZero() {
        assertThat(Money.add(null, new BigDecimal("100"))).isEqualByComparingTo("100.00");
        assertThat(Money.subtract(null, null)).isEqualByComparingTo("0.00");
        assertThat(Money.multiply(null, null)).isEqualByComparingTo("0.00");
    }
}
