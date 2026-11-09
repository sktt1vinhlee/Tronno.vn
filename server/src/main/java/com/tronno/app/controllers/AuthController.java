package com.tronno.app.controllers;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tronno.app.dtos.AuthDtos.*;
import com.tronno.app.service.AuthService;

/**
 * AuthController
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    /**
     * Đăng ký tài khoản mới.
     *
     * @param r thông tin đăng ký
     * @return AuthResponse thông tin phiên đăng nhập
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }

    /**
     * Đăng nhập bằng email và mật khẩu.
     *
     * @param r thông tin đăng nhập
     * @return AuthResponse thông tin phiên đăng nhập
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    /**
     * Lấy thông tin tài khoản hiện tại.
     *
     * @param a đối tượng xác thực
     * @return AuthResponse thông tin tài khoản
     */
    @GetMapping("/me")
    public AuthResponse me(Authentication a) {
        return auth.me((String) a.getPrincipal());
    }

    /**
     * Cập nhật hồ sơ cá nhân.
     *
     * @param a đối tượng xác thực
     * @param r thông tin cần cập nhật
     * @return AuthResponse thông tin tài khoản đã cập nhật
     */
    @PutMapping("/me")
    public AuthResponse updateMe(Authentication a, @Valid @RequestBody UpdateProfileRequest r) {
        return auth.updateProfile((String) a.getPrincipal(), r);
    }

    /**
     * Cập nhật hồ sơ cá nhân.
     *
     * @param a đối tượng xác thực
     * @param r thông tin cần cập nhật
     * @return AuthResponse thông tin tài khoản đã cập nhật
     */
    @PutMapping("/profile")
    public AuthResponse updateProfile(Authentication a, @Valid @RequestBody UpdateProfileRequest r) {
        return auth.updateProfile((String) a.getPrincipal(), r);
    }
}
