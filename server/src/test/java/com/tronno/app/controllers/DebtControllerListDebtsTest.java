package com.tronno.app.controllers;

import java.util.List;

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
import com.tronno.app.models.DebtStatus;
import com.tronno.app.service.DebtService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;
import static com.tronno.app.controllers.DebtControllerResponseFactory.response;

/**
 * DebtControllerListDebtsTest
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
@DisplayName("DebtController GET /api/debts")
public class DebtControllerListDebtsTest {

    @Mock
    private DebtService debtService;

    private MockMvc mockMvc;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DebtController(debtService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        auth = ControllerTestData.auth();
    }

    @Test
    @DisplayName("Trả về danh sách khoản nợ")
    void returnsList() throws Exception {
        when(debtService.listDebts(eq(USER_ID), isNull())).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/debts").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(DEBT_ID))
                .andExpect(jsonPath("$[0].name").value("Vay mua xe"));
    }

    @Test
    @DisplayName("Lọc theo trạng thái ACTIVE")
    void filtersByStatus() throws Exception {
        when(debtService.listDebts(eq(USER_ID), eq(DebtStatus.ACTIVE)))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/debts").param("status", "ACTIVE").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(debtService).listDebts(USER_ID, DebtStatus.ACTIVE);
    }
}
