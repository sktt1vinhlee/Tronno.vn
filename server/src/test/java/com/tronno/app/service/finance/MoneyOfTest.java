package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MoneyOfTest
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
@DisplayName("Money.of() - Chuyển đổi về BigDecimal chuẩn hoá")
public class MoneyOfTest {

    @Test
    @DisplayName("null trả về 0.00")
    void nullReturnsZero() {
        assertThat(Money.of(null)).isEqualByComparingTo("0.00");
        assertThat(Money.of(null).scale()).isEqualTo(Money.SCALE);
    }

    @Test
    @DisplayName("BigDecimal được giữ nguyên giá trị, chuẩn hoá 2 chữ số thập phân")
    void bigDecimalKeepsValue() {
        assertThat(Money.of(new BigDecimal("1234.567"))).isEqualByComparingTo("1234.57");
    }

    @Test
    @DisplayName("Chuỗi số được chuyển đổi sang BigDecimal")
    void stringIsConverted() {
        assertThat(Money.of("5000")).isEqualByComparingTo("5000.00");
        assertThat(Money.of("5000.5")).isEqualByComparingTo("5000.50");
    }

    @Test
    @DisplayName("Số nguyên kiểu Integer/Long được chuyển đổi")
    void integerTypesAreConverted() {
        assertThat(Money.of(1000)).isEqualByComparingTo("1000.00");
        assertThat(Money.of(1000L)).isEqualByComparingTo("1000.00");
    }
}
