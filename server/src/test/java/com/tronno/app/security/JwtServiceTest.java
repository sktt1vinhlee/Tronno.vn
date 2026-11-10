package com.tronno.app.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtServiceTest
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
@DisplayName("JwtService - Sinh và xác thực token")
class JwtServiceTest {

    private static final String SECRET = "tronno-test-secret-key-at-least-32-chars-long";

    private JwtService service() {
        JwtService s = new JwtService();
        ReflectionTestUtils.setField(s, "secret", SECRET);
        ReflectionTestUtils.setField(s, "expirationMs", 3600000L);
        return s;
    }

    @Test
    @DisplayName("Token sinh ra chứa đúng email người dùng")
    void tokenContainsEmail() {
        JwtService s = service();

        String token = s.generate("vinh@example.com");

        assertThat(token).isNotBlank();
        assertThat(s.extractEmail(token)).isEqualTo("vinh@example.com");
    }

    @Test
    @DisplayName("Email viết hoa/thường được giữ nguyên trong token")
    void emailCaseIsPreserved() {
        JwtService s = service();

        String token = s.generate("Vinh@Example.com");

        assertThat(s.extractEmail(token)).isEqualTo("Vinh@Example.com");
    }

    @Test
    @DisplayName("Token không hợp lệ bị từ chối")
    void invalidTokenIsRejected() {
        JwtService s = service();

        assertThatThrownBy(() -> s.extractEmail("khong-phai-token-hop-le"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Token ký bằng khoá khác bị từ chối")
    void tokenSignedWithOtherKeyIsRejected() {
        JwtService s = service();

        JwtService other = new JwtService();
        ReflectionTestUtils.setField(other, "secret", "mot-khoa-khac-hoan-toan-khac-32-chars");
        ReflectionTestUtils.setField(other, "expirationMs", 3600000L);

        String foreignToken = other.generate("vinh@example.com");

        assertThatThrownBy(() -> s.extractEmail(foreignToken)).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Token đã hết hạn bị từ chối")
    void expiredTokenIsRejected() {
        JwtService s = new JwtService();
        ReflectionTestUtils.setField(s, "secret", SECRET);
        ReflectionTestUtils.setField(s, "expirationMs", -1000L);

        String expired = s.generate("vinh@example.com");

        assertThatThrownBy(() -> s.extractEmail(expired)).isInstanceOf(Exception.class);
    }
}
