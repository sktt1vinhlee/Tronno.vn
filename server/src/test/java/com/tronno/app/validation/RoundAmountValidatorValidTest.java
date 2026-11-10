package com.tronno.app.validation;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RoundAmountValidatorValidTest
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
@DisplayName("RoundAmountValidator - Số tiền hợp lệ")
class RoundAmountValidatorValidTest {

    private final RoundAmountValidator validator = new RoundAmountValidator();

    @Test
    @DisplayName("null được coi là hợp lệ (để @NotNull loại trách nhiệm)")
    void nullIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("Số tiền chia hết cho 1000 là hợp lệ")
    void multiplesOfOneThousandAreValid() {
        assertThat(validator.isValid(new BigDecimal("1000"), null)).isTrue();
        assertThat(validator.isValid(new BigDecimal("5000000"), null)).isTrue();
        assertThat(validator.isValid(new BigDecimal("12000000.00"), null)).isTrue();
    }

    @Test
    @DisplayName("Số tiền có phần thập phân 0 vẫn hợp lệ nếu chia hết cho 1000")
    void trailingZerosAreValid() {
        assertThat(validator.isValid(new BigDecimal("1000.00"), null)).isTrue();
    }
}
