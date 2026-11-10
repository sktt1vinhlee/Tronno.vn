package com.tronno.app.service;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.tronno.app.dtos.AuthDtos.AuthResponse;
import com.tronno.app.dtos.AuthDtos.LoginRequest;
import com.tronno.app.models.User;
import com.tronno.app.repository.UserRepository;
import com.tronno.app.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.AuthServiceTestData.EMAIL;
import static com.tronno.app.service.AuthServiceTestData.user;

/**
 * AuthServiceLoginTest
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
@DisplayName("AuthService.login() - Đăng nhập")
public class AuthServiceLoginTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private AuthService service;

    @Test
    @DisplayName("Đăng nhập đúng mật khẩu trả về token")
    void loginSucceeds() {
        LoginRequest r = new LoginRequest(EMAIL, "Abc@1234");

        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(encoder.matches("Abc@1234", "encoded")).thenReturn(true);
        when(jwt.generate(EMAIL)).thenReturn("jwt-token");

        AuthResponse res = service.login(r);

        assertThat(res.token()).isEqualTo("jwt-token");
        assertThat(res.email()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("Email không tồn tại thì báo lỗi UNAUTHORIZED")
    void unknownEmailFails() {
        LoginRequest r = new LoginRequest("khong@example.com", "Abc@1234");

        when(users.findByEmail("khong@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(r))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("không đúng");
    }

    @Test
    @DisplayName("Sai mật khẩu thì báo lỗi UNAUTHORIZED")
    void wrongPasswordFails() {
        LoginRequest r = new LoginRequest(EMAIL, "SaiMatKhau@1");

        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(encoder.matches("SaiMatKhau@1", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> service.login(r))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("không đúng");
    }

    @Test
    @DisplayName("Tài khoản bị khoá thì báo lỗi FORBIDDEN")
    void blockedAccountFails() {
        User blocked = user();
        blocked.setBlocked(true);
        LoginRequest r = new LoginRequest(EMAIL, "Abc@1234");

        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(blocked));
        when(encoder.matches("Abc@1234", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> service.login(r))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bị khoá");
    }
}
