package com.tronno.app.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.tronno.app.dtos.AuthDtos.AuthResponse;
import com.tronno.app.dtos.AuthDtos.RegisterRequest;
import com.tronno.app.models.User;
import com.tronno.app.repository.UserRepository;
import com.tronno.app.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.AuthServiceTestData.EMAIL;
import static com.tronno.app.service.AuthServiceTestData.registerRequest;

/**
 * AuthServiceRegisterTest
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
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService.register() - Đăng ký tài khoản")
public class AuthServiceRegisterTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private AuthService service;

    @Test
    @DisplayName("Đăng ký thành công tạo user và trả về token")
    void registersSuccessfully() {
        RegisterRequest r = registerRequest("Vinh@Example.com", "Abc@1234");

        when(users.existsByEmail(EMAIL)).thenReturn(false);
        when(encoder.encode("Abc@1234")).thenReturn("encoded");
        when(jwt.generate(EMAIL)).thenReturn("jwt-token");
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        AuthResponse res = service.register(r);

        assertThat(res.token()).isEqualTo("jwt-token");
        assertThat(res.email()).isEqualTo(EMAIL);
        assertThat(res.fullName()).isEqualTo("Hà Lê Vinh");
        assertThat(res.role()).isEqualTo("MEMBER");
        verify(users).save(any(User.class));
    }

    @Test
    @DisplayName("Email đã tồn tại thì báo lỗi BAD_REQUEST")
    void duplicateEmailFails() {
        RegisterRequest r = new RegisterRequest(
                "Hà Lê Vinh", EMAIL, null, "Abc@1234", "Abc@1234", null);

        when(users.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.register(r))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("đã được đăng ký");

        verify(users, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký với thu nhập tháng vẫn trả về hồ sơ đầy đủ")
    void keepsMonthlyIncome() {
        RegisterRequest r = new RegisterRequest(
                "Hà Lê Vinh", EMAIL, "0900000000", "Abc@1234", "Abc@1234",
                new BigDecimal("15000000"));

        when(users.existsByEmail(EMAIL)).thenReturn(false);
        when(encoder.encode("Abc@1234")).thenReturn("encoded");
        when(jwt.generate(EMAIL)).thenReturn("jwt-token");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse res = service.register(r);

        assertThat(res.monthlyIncome()).isEqualByComparingTo("15000000");
    }
}
