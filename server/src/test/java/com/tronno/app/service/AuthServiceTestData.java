package com.tronno.app.service;

import java.math.BigDecimal;

import com.tronno.app.models.User;
import com.tronno.app.models.UserRole;

/**
 * AuthServiceTestData
 *
 * <p>Bộ dữ liệu dùng chung cho các test của {@link AuthService}.</p>
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
public final class AuthServiceTestData {

    /** Email dùng cho các test. */
    public static final String EMAIL = "vinh@example.com";

    private AuthServiceTestData() {
    }

    /**
     * Tạo user mẫu đã đăng nhập được.
     *
     * @return user mẫu
     */
    public static User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setPassword("encoded");
        user.setFullName("Hà Lê Vinh");
        user.setRole(UserRole.MEMBER);
        user.setBlocked(false);
        return user;
    }

    /**
     * Tạo request đăng ký mẫu.
     *
     * @param email    email đăng ký
     * @param password mật khẩu
     * @return request đăng ký
     */
    public static com.tronno.app.dtos.AuthDtos.RegisterRequest registerRequest(
            String email, String password) {
        return new com.tronno.app.dtos.AuthDtos.RegisterRequest(
                "Hà Lê Vinh", email, "0900000000", password, password,
                new BigDecimal("15000000"));
    }
}
