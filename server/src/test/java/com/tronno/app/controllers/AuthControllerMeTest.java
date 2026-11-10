package com.tronno.app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tronno.app.exception.GlobalExceptionHandler;
import com.tronno.app.service.AuthService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.AuthControllerResponseFactory.response;

/**
 * AuthControllerMeTest
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
@DisplayName("AuthController GET /api/auth/me")
public class AuthControllerMeTest {

    @Mock
    private AuthService auth;

    private MockMvc mockMvc;
    private Authentication authPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(auth))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authPrincipal = ControllerTestData.auth();
    }

    @Test
    @DisplayName("Trả về thông tin tài khoản đang đăng nhập")
    void returnsCurrentUser() throws Exception {
        when(auth.me("vinh@example.com")).thenReturn(response(null));

        mockMvc.perform(get("/api/auth/me").principal(authPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("vinh@example.com"))
                .andExpect(jsonPath("$.fullName").value("Hà Lê Vinh"));
    }
}
