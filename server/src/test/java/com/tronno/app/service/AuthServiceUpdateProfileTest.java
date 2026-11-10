package com.tronno.app.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tronno.app.dtos.AuthDtos.AuthResponse;
import com.tronno.app.dtos.AuthDtos.UpdateProfileRequest;
import com.tronno.app.models.User;
import com.tronno.app.repository.UserRepository;
import com.tronno.app.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.AuthServiceTestData.EMAIL;
import static com.tronno.app.service.AuthServiceTestData.user;

/**
 * AuthServiceUpdateProfileTest
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
@DisplayName("AuthService.updateProfile() - Cập nhật hồ sơ")
class AuthServiceUpdateProfileTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private AuthService service;

    @Test
    @DisplayName("Cập nhật họ tên và số điện thoại")
    void updatesProfile() {
        UpdateProfileRequest r = new UpdateProfileRequest(
                "Hà Lê Vinh", "0900000000", "Hà Nội", new BigDecimal("20000000"));

        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse res = service.updateProfile(EMAIL, r);

        assertThat(res.fullName()).isEqualTo("Hà Lê Vinh");
        assertThat(res.phone()).isEqualTo("0900000000");
        assertThat(res.address()).isEqualTo("Hà Nội");
        assertThat(res.monthlyIncome()).isEqualByComparingTo("20000000");
    }

    @Test
    @DisplayName("Chuỗi rỗng được chuẩn hoá thành null")
    void blankValuesBecomeNull() {
        UpdateProfileRequest r = new UpdateProfileRequest(
                "Hà Lê Vinh", "", "", null);

        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse res = service.updateProfile(EMAIL, r);

        assertThat(res.phone()).isNull();
        assertThat(res.address()).isNull();
    }
}
