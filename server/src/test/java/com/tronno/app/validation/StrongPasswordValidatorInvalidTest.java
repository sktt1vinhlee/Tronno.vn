package com.tronno.app.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StrongPasswordValidatorInvalidTest
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
@DisplayName("StrongPasswordValidator - Mật khẩu không hợp lệ")
class StrongPasswordValidatorInvalidTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    @DisplayName("Mật khẩu ngắn hơn 8 ký tự không hợp lệ")
    void tooShortIsInvalid() {
        assertThat(validator.isValid("Ab@123", null)).isFalse();
    }

    @Test
    @DisplayName("Mật khẩu thiếu chữ hoa không hợp lệ")
    void missingUpperCaseIsInvalid() {
        assertThat(validator.isValid("abc@1234", null)).isFalse();
    }

    @Test
    @DisplayName("Mật khẩu thiếu chữ thường không hợp lệ")
    void missingLowerCaseIsInvalid() {
        assertThat(validator.isValid("ABC@1234", null)).isFalse();
    }

    @Test
    @DisplayName("Mật khẩu thiếu chữ số không hợp lệ")
    void missingDigitIsInvalid() {
        assertThat(validator.isValid("Abcdef@#", null)).isFalse();
    }

    @Test
    @DisplayName("Mật khẩu thiếu ký tự đặc biệt không hợp lệ")
    void missingSpecialIsInvalid() {
        assertThat(validator.isValid("Abcdef123", null)).isFalse();
    }
}
