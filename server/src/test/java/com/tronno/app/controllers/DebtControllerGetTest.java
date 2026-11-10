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
import com.tronno.app.exception.ResourceNotFoundException;
import com.tronno.app.service.DebtService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;
import static com.tronno.app.controllers.DebtControllerResponseFactory.response;

/**
 * DebtControllerGetTest
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
@DisplayName("DebtController GET /api/debts/{id}")
class DebtControllerGetTest {

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
    @DisplayName("Trả về chi tiết khoản nợ")
    void returnsDetail() throws Exception {
        when(debtService.getDebt(USER_ID, DEBT_ID)).thenReturn(response());

        mockMvc.perform(get("/api/debts/{id}", DEBT_ID).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vay mua xe"))
                .andExpect(jsonPath("$.principal").value(12000000));
    }

    @Test
    @DisplayName("Khoản nợ không tồn tại trả về 404")
    void unknownDebtReturns404() throws Exception {
        when(debtService.getDebt(USER_ID, DEBT_ID))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy khoản nợ"));

        mockMvc.perform(get("/api/debts/{id}", DEBT_ID).principal(auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Không tìm thấy khoản nợ"));
    }
}
