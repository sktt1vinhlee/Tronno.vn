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
import com.tronno.app.models.DebtStatus;
import com.tronno.app.service.DebtService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;
import static com.tronno.app.controllers.DebtControllerResponseFactory.response;

/**
 * DebtControllerChangeStatusTest
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
@DisplayName("DebtController PATCH /api/debts/{id}/status")
public class DebtControllerChangeStatusTest {

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
    @DisplayName("Đổi trạng thái khoản nợ thành công")
    void changesStatus() throws Exception {
        when(debtService.getDebt(USER_ID, DEBT_ID)).thenReturn(response());

        mockMvc.perform(patch("/api/debts/{id}/status", DEBT_ID)
                        .param("status", "PAID")
                        .principal(auth))
                .andExpect(status().isOk());

        verify(debtService).closeDebt(USER_ID, DEBT_ID, DebtStatus.PAID);
    }

    @Test
    @DisplayName("Thiếu tham số status trả về 400")
    void missingStatusReturns400() throws Exception {
        mockMvc.perform(patch("/api/debts/{id}/status", DEBT_ID).principal(auth))
                .andExpect(status().isBadRequest());
    }
}
