package com.tronno.app.exception;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandlerTest
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
@DisplayName("GlobalExceptionHandler - Chuyển đổi lỗi thành phản hồi HTTP")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Lỗi 404 trả về đúng mã và thông báo")
    void notFoundReturns404() {
        ResponseEntity<Map<String, String>> res =
                handler.handleNotFound(new ResourceNotFoundException("Không tìm thấy khoản nợ"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).containsEntry("message", "Không tìm thấy khoản nợ");
    }

    @Test
    @DisplayName("Lỗi nghiệp vụ trả về 400 kèm thông báo trong errors")
    void businessErrorReturns400() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleBusiness(new IllegalArgumentException("Khoản nợ đang còn hiệu lực"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).containsEntry("message", "Dữ liệu không hợp lệ");
        assertThat(res.getBody()).containsEntry("status", 400);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) res.getBody().get("errors");
        assertThat(errors).containsEntry("request", "Khoản nợ đang còn hiệu lực");
    }

    @Test
    @DisplayName("Lỗi nghiệp vụ không có thông báo thì dùng câu mặc định")
    void businessErrorWithoutMessageUsesDefault() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleBusiness(new IllegalArgumentException());

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) res.getBody().get("errors");
        assertThat(errors).containsEntry("request", "Yêu cầu không hợp lệ");
    }

    @Test
    @DisplayName("ResponseStatusException 400 được chuyển thành lỗi validation")
    void badRequestStatusBecomesValidationError() {
        ResponseEntity<Map<String, Object>> res = handler.handleStatus(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email này đã được đăng ký"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) res.getBody().get("errors");
        assertThat(errors).containsEntry("request", "Email này đã được đăng ký");
    }

    @Test
    @DisplayName("ResponseStatusException 401 giữ nguyên mã trạng thái")
    void unauthorizedStatusIsPreserved() {
        ResponseEntity<Map<String, Object>> res = handler.handleStatus(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody()).containsEntry("message", "Chưa đăng nhập");
    }

    @Test
    @DisplayName("ResponseStatusException 403 giữ nguyên mã trạng thái")
    void forbiddenStatusIsPreserved() {
        ResponseEntity<Map<String, Object>> res = handler.handleStatus(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khoá"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody()).containsEntry("message", "Tài khoản của bạn đã bị khoá");
    }
}
