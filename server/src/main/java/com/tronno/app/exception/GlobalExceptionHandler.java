package com.tronno.app.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * GlobalExceptionHandler
 *
 * Version 1.0
 *
 * Date: 09-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 09-11-2026         VinhHL             Create
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Xử lý lỗi có mã HTTP định sẵn.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException e) {
        if (e.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()) {
            String message = e.getReason() == null ? "Yêu cầu không hợp lệ" : e.getReason();
            return validationError(Map.of("request", message));
        }
        String message = e.getReason() == null ? "Request failed" : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", message));
    }

    /**
     * Xử lý lỗi không tìm thấy tài nguyên.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /**
     * Xử lý lỗi nghiệp vụ.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(IllegalArgumentException e) {
        return validationError(Map.of("request", e.getMessage() == null
                ? "Yêu cầu không hợp lệ" : e.getMessage()));
    }

    /**
     * Xử lý lỗi kiểm tra dữ liệu đầu vào.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return validationError(errors);
    }

    /**
     * Xử lý lỗi vi phạm ràng buộc.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getConstraintViolations()
                .forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return validationError(errors);
    }

    /**
     * Xử lý lỗi khi không đọc được body của request.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException e) {
        return invalidRequest("Request body thiếu hoặc không phải JSON hợp lệ cho endpoint này");
    }

    /**
     * Xử lý lỗi kiểu tham số không khớp.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return invalidRequest("Tham số '" + e.getName() + "' có giá trị không hợp lệ");
    }

    /**
     * Xử lý lỗi thiếu tham số bắt buộc.
     *
     * @param e lỗi phát sinh
     * @return ResponseEntity phản hồi lỗi
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException e) {
        return invalidRequest("Tham số '" + e.getParameterName() + "' là bắt buộc");
    }

    /**
     * Tạo phản hồi lỗi cho danh sách lỗi kiểm tra dữ liệu.
     *
     * @param errors danh sách lỗi trường
     * @return ResponseEntity phản hồi lỗi
     */
    private static ResponseEntity<Map<String, Object>> validationError(Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Dữ liệu không hợp lệ");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Tạo phản hồi lỗi cho yêu cầu không hợp lệ.
     *
     * @param message thông báo lỗi
     * @return ResponseEntity phản hồi lỗi
     */
    private static ResponseEntity<Map<String, String>> invalidRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("code", "INVALID_REQUEST", "message", message));
    }
}
