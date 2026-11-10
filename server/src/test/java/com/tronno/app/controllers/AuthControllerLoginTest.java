package com.tronno.app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tronno.app.exception.GlobalExceptionHandler;
import com.tronno.app.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.AuthControllerResponseFactory.response;

/**
 * AuthControllerLoginTest
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
@DisplayName("AuthController POST /api/auth/login")
class AuthControllerLoginTest {

    @Mock
    private AuthService auth;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(auth))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Đăng nhập thành công trả về token")
    void loginSucceeds() throws Exception {
        when(auth.login(any())).thenReturn(response("jwt-token"));

        String body = """
                {
                  "email": "vinh@example.com",
                  "password": "Abc@1234"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("Thiếu mật khẩu bị từ chối")
    void missingPasswordReturns400() throws Exception {
        String body = """
                {
                  "email": "vinh@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
