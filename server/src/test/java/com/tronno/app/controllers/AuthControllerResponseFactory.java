package com.tronno.app.controllers;

import java.math.BigDecimal;

import com.tronno.app.dtos.AuthDtos.AuthResponse;

/**
 * AuthControllerResponseFactory
 *
 * <p>Tạo dữ liệu mẫu cho các test của {@link AuthController}.</p>
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
public final class AuthControllerResponseFactory {

    private AuthControllerResponseFactory() {
    }

    /**
     * Tạo response xác thực mẫu.
     *
     * @param token token JWT, có thể null
     * @return response xác thực mẫu
     */
    public static AuthResponse response(String token) {
        return new AuthResponse(token, 1L, "vinh@example.com", "Hà Lê Vinh", "MEMBER",
                "0900000000", "Hà Nội", null, new BigDecimal("15000000"));
    }
}
