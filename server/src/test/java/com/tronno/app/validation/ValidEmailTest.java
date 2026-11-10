package com.tronno.app.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ValidEmailTest
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
@DisplayName("ValidEmail - Kiểm tra định dạng email")
public class ValidEmailTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("Email hợp lệ không báo lỗi")
    void validEmailPasses() {
        assertThat(validator.validate(new EmailSample("vinh@example.com"))).isEmpty();
        assertThat(validator.validate(new EmailSample("a.b+c@sub.domain.co"))).isEmpty();
    }

    @Test
    @DisplayName("Email thiếu @ báo lỗi")
    void missingAtSignFails() {
        assertThat(validator.validate(new EmailSample("vinh.example.com"))).isNotEmpty();
    }

    @Test
    @DisplayName("Email thiếu tên miền báo lỗi")
    void missingDomainFails() {
        assertThat(validator.validate(new EmailSample("vinh@"))).isNotEmpty();
    }

    @Test
    @DisplayName("Email rỗng báo lỗi")
    void blankEmailFails() {
        assertThat(validator.validate(new EmailSample(""))).isNotEmpty();
        assertThat(validator.validate(new EmailSample(null))).isNotEmpty();
    }

    @Test
    @DisplayName("Email có khoảng trắng báo lỗi")
    void emailWithSpacesFails() {
        assertThat(validator.validate(new EmailSample("vinh h@example.com"))).isNotEmpty();
    }
}
