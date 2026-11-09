package com.tronno.app.service;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tronno.app.dtos.AuthDtos.*;
import com.tronno.app.models.User;
import com.tronno.app.models.UserRole;
import com.tronno.app.repository.UserRepository;
import com.tronno.app.security.JwtService;

/**
 * AuthService
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
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    /**
     * Tạo lỗi có mã HTTP để trả về cho client.
     *
     * @param status mã lỗi HTTP
     * @param message thông báo lỗi
     * @return ResponseStatusException lỗi tương ứng
     */
    private ResponseStatusException err(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }

    /**
     * Chuyển đổi entity User thành DTO trả về cho client.
     *
     * @param u entity người dùng
     * @param token token đăng nhập, có thể null
     * @return AuthResponse thông tin tài khoản
     */
    private AuthResponse toResponse(User u, String token) {
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getFullName(),
                u.getRole().name(), u.getPhone(), u.getAddress(), u.getAvatarUrl(),
                u.getMonthlyIncome());
    }

    /**
     * Đăng ký tài khoản mới cho người dùng.
     *
     * @param r thông tin đăng ký
     * @return AuthResponse thông tin phiên đăng nhập
     */
    @Transactional
    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw err(HttpStatus.BAD_REQUEST, "Email này đã được đăng ký");
        }
        User u = new User();
        u.setEmail(email);
        u.setFullName(r.fullName().trim());
        u.setPhone(r.phone() == null || r.phone().isBlank() ? null : r.phone().trim());
        u.setPassword(encoder.encode(r.password()));
        u.setRole(UserRole.MEMBER);
        if (r.monthlyIncome() != null) u.setMonthlyIncome(r.monthlyIncome());
        users.save(u);
        return toResponse(u, jwt.generate(u.getEmail()));
    }

    /**
     * Đăng nhập bằng email và mật khẩu.
     *
     * @param r thông tin đăng nhập
     * @return AuthResponse thông tin phiên đăng nhập
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .orElseThrow(() -> err(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"));
        if (!encoder.matches(r.password(), u.getPassword())) {
            throw err(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng");
        }
        if (!u.isActive()) {
            throw err(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khoá");
        }
        return toResponse(u, jwt.generate(u.getEmail()));
    }

    /**
     * Lấy thông tin tài khoản hiện tại.
     *
     * @param email email người dùng
     * @return AuthResponse thông tin tài khoản
     */
    @Transactional(readOnly = true)
    public AuthResponse me(String email) {
        User u = users.findByEmail(email)
                .orElseThrow(() -> err(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"));
        return toResponse(u, null);
    }

    /**
     * Cập nhật hồ sơ cá nhân của người dùng.
     *
     * @param email email người dùng
     * @param r thông tin cần cập nhật
     * @return AuthResponse thông tin tài khoản đã cập nhật
     */
    @Transactional
    public AuthResponse updateProfile(String email, UpdateProfileRequest r) {
        User u = users.findByEmail(email)
                .orElseThrow(() -> err(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"));
        u.setFullName(r.fullName().trim());
        u.setPhone(blankToNull(r.phone()));
        u.setAddress(blankToNull(r.address()));
        if (r.monthlyIncome() != null) u.setMonthlyIncome(r.monthlyIncome());
        users.save(u);
        return toResponse(u, null);
    }

    /**
     * Chuyển chuỗi rỗng thành null để lưu vào database.
     *
     * @param value chuỗi cần chuẩn hoá
     * @return String đã chuẩn hoá
     */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
