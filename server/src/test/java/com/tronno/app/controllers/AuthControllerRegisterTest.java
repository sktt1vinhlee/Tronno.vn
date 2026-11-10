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
 * AuthControllerRegisterTest
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
@DisplayName("AuthController POST /api/auth/register")
class AuthControllerRegisterTest {

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
    @DisplayName("Đăng ký hợp lệ trả về token")
    void registersSuccessfully() throws Exception {
        when(auth.register(any())).thenReturn(response("jwt-token"));

        String body = """
                {
                  "fullName": "Hà Lê Vinh",
                  "email": "vinh@example.com",
                  "password": "Abc@1234",
                  "confirmPassword": "Abc@1234"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("vinh@example.com"));
    }

    @Test
    @DisplayName("Mật khẩu yếu bị từ chối")
    void weakPasswordReturns400() throws Exception {
        String body = """
                {
                  "fullName": "Hà Lê Vinh",
                  "email": "vinh@example.com",
                  "password": "abc",
                  "confirmPassword": "abc"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Mật khẩu xác nhận không khớp bị từ chối")
    void mismatchedPasswordReturns400() throws Exception {
        String body = """
                {
                  "fullName": "Hà Lê Vinh",
                  "email": "vinh@example.com",
                  "password": "Abc@1234",
                  "confirmPassword": "Abc@9999"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Email không hợp lệ bị từ chối")
    void invalidEmailReturns400() throws Exception {
        String body = """
                {
                  "fullName": "Hà Lê Vinh",
                  "email": "khong-phai-email",
                  "password": "Abc@1234",
                  "confirmPassword": "Abc@1234"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }
}
