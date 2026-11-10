package com.tronno.app.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StrongPasswordValidatorValidTest
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
@DisplayName("StrongPasswordValidator - Mật khẩu hợp lệ")
class StrongPasswordValidatorValidTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    @DisplayName("null hoặc rỗng được coi là hợp lệ (để @NotBlank loại trách nhiệm)")
    void blankIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isTrue();
        assertThat(validator.isValid("   ", null)).isTrue();
    }

    @Test
    @DisplayName("Mật khẩu có đủ hoa, thường, số và ký tự đặc biệt là hợp lệ")
    void strongPasswordIsValid() {
        assertThat(validator.isValid("Abc@1234", null)).isTrue();
        assertThat(validator.isValid("P@ssw0rd!", null)).isTrue();
        assertThat(validator.isValid("Tr0nno#2026$", null)).isTrue();
    }
}
