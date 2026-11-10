package com.tronno.app.validation;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RoundAmountValidatorInvalidTest
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
@DisplayName("RoundAmountValidator - Số tiền không hợp lệ")
public class RoundAmountValidatorInvalidTest {

    private final RoundAmountValidator validator = new RoundAmountValidator();

    @Test
    @DisplayName("Số tiền nhỏ hơn 1.000 đồng không hợp lệ")
    void belowMinimumIsInvalid() {
        assertThat(validator.isValid(new BigDecimal("500"), null)).isFalse();
        assertThat(validator.isValid(new BigDecimal("999.99"), null)).isFalse();
    }

    @Test
    @DisplayName("Số tiền không chia hết cho 1000 không hợp lệ")
    void notMultipleOfOneThousandIsInvalid() {
        assertThat(validator.isValid(new BigDecimal("1500"), null)).isFalse();
        assertThat(validator.isValid(new BigDecimal("1000.50"), null)).isFalse();
        assertThat(validator.isValid(new BigDecimal("1234567"), null)).isFalse();
    }

    @Test
    @DisplayName("Số tiền âm không hợp lệ")
    void negativeIsInvalid() {
        assertThat(validator.isValid(new BigDecimal("-1000"), null)).isFalse();
    }
}
