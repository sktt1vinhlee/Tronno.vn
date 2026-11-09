package com.tronno.app.security;

import org.springframework.security.core.Authentication;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Long id(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object details = authentication.getDetails();
        if (details instanceof Long id) return id;
        return null;
    }

    public static String email(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal == null) return null;
        String value = principal.toString();
        return value.isBlank() ? null : value;
    }
}
