package com.tronno.app.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

import com.tronno.app.validation.StrongPassword;
import com.tronno.app.validation.ValidEmail;

/**
 * AuthDtos
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
public class AuthDtos {
    public record RegisterRequest(
            @NotBlank(message = "Vui lòng nhập họ tên")
            @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
            String fullName,
            @ValidEmail String email,
            @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
            String phone,
            @NotBlank(message = "Vui lòng nhập mật khẩu")
            @StrongPassword String password,
            @NotBlank(message = "Vui lòng xác nhận mật khẩu")
            String confirmPassword,
            @DecimalMin(value = "0.00", message = "Thu nhập không được âm")
            BigDecimal monthlyIncome) {
        @AssertTrue(message = "Mật khẩu xác nhận không khớp")
        public boolean isPasswordConfirmed() {
            return password != null && password.equals(confirmPassword);
        }
    }

    public record LoginRequest(
            @ValidEmail String email,
            @NotBlank(message = "Vui lòng nhập mật khẩu") String password) {}

    public record AuthResponse(
            String token,
            Long id,
            String email,
            String fullName,
            String role,
            String phone,
            String address,
            String avatarUrl,
            BigDecimal monthlyIncome) {}

    public record UpdateProfileRequest(
            @NotBlank(message = "Vui lòng nhập họ tên")
            @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
            String fullName,
            @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
            String phone,
            @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
            String address,
            @DecimalMin(value = "0.00", message = "Thu nhập không được âm")
            BigDecimal monthlyIncome) {}
}
