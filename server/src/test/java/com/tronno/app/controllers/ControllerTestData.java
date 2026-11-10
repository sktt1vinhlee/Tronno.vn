package com.tronno.app.controllers;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * ControllerTestData
 *
 * <p>Bộ dữ liệu dùng chung cho các test controller.</p>
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
public final class ControllerTestData {

    /** Id người dùng sử dụng trong test. */
    public static final Long USER_ID = 1L;

    /** Id khoản nợ sử dụng trong test. */
    public static final Long DEBT_ID = 10L;

    /** Email người dùng sử dụng trong test. */
    public static final String EMAIL = "vinh@example.com";

    private ControllerTestData() {
    }

    /**
     * Tạo principal đã xác thực với id người dùng trong details.
     *
     * @return principal đã xác thực
     */
    public static Authentication auth() {
        Authentication auth = new UsernamePasswordAuthenticationToken(EMAIL, null, List.of());
        ((UsernamePasswordAuthenticationToken) auth).setDetails(USER_ID);
        return auth;
    }
}
