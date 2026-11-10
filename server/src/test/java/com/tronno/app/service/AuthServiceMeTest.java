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
import com.tronno.app.repository.UserRepository;
import com.tronno.app.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.AuthServiceTestData.EMAIL;
import static com.tronno.app.service.AuthServiceTestData.user;

/**
 * AuthServiceMeTest
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
@DisplayName("AuthService.me() - Thông tin tài khoản")
class AuthServiceMeTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private AuthService service;

    @Test
    @DisplayName("Trả về thông tin user, không kèm token")
    void returnsUserInfo() {
        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user()));

        AuthResponse res = service.me(EMAIL);

        assertThat(res.token()).isNull();
        assertThat(res.email()).isEqualTo(EMAIL);
        assertThat(res.fullName()).isEqualTo("Hà Lê Vinh");
    }

    @Test
    @DisplayName("Chưa đăng nhập thì báo lỗi UNAUTHORIZED")
    void notLoggedInFails() {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.me("khong@example.com"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
