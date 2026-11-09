package com.tronno.app.security;

import org.springframework.security.core.Authentication;

/**
 * CurrentUser
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
public final class CurrentUser {
    private CurrentUser() {
    }

    /**
     * Lấy id người dùng từ đối tượng xác thực.
     *
     * @param authentication đối tượng xác thực của Spring Security
     * @return Long id người dùng, có thể null
     */
    public static Long id(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object details = authentication.getDetails();
        if (details instanceof Long id) return id;
        return null;
    }

    /**
     * Lấy email người dùng từ đối tượng xác thực.
     *
     * @param authentication đối tượng xác thực của Spring Security
     * @return String email người dùng, có thể null
     */
    public static String email(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal == null) return null;
        String value = principal.toString();
        return value.isBlank() ? null : value;
    }
}
