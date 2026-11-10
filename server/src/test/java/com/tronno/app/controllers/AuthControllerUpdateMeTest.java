package com.tronno.app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tronno.app.exception.GlobalExceptionHandler;
import com.tronno.app.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.AuthControllerResponseFactory.response;

/**
 * AuthControllerUpdateMeTest
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
@DisplayName("AuthController PUT /api/auth/me")
class AuthControllerUpdateMeTest {

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
    @DisplayName("Cập nhật hồ sơ thành công")
    void updatesProfile() throws Exception {
        when(auth.updateProfile(eq("vinh@example.com"), any())).thenReturn(response(null));

        String body = """
                {
                  "fullName": "Hà Lê Vinh",
                  "phone": "0900000000",
                  "address": "Hà Nội"
                }
                """;

        mockMvc.perform(put("/api/auth/me").principal(authPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Hà Nội"));
    }
}
