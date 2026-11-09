package com.tronno.app.service;

import com.tronno.app.dtos.AuthDtos.*;
import com.tronno.app.models.User;
import com.tronno.app.models.UserRole;
import com.tronno.app.repository.UserRepository;
import com.tronno.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    private ResponseStatusException err(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }

    private AuthResponse toResponse(User u, String token) {
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getFullName(),
                u.getRole().name(), u.getPhone(), u.getAddress(), u.getAvatarUrl(),
                u.getMonthlyIncome());
    }

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

    @Transactional(readOnly = true)
    public AuthResponse me(String email) {
        User u = users.findByEmail(email)
                .orElseThrow(() -> err(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"));
        return toResponse(u, null);
    }

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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
