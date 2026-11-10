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
import com.tronno.app.service.DebtService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;

/**
 * DebtControllerDeleteTest
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
@DisplayName("DebtController DELETE /api/debts/{id}")
public class DebtControllerDeleteTest {

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
    @DisplayName("Xoá khoản nợ thành công")
    void deletesDebt() throws Exception {
        mockMvc.perform(delete("/api/debts/{id}", DEBT_ID).principal(auth))
                .andExpect(status().isOk());

        verify(debtService).deleteDebt(USER_ID, DEBT_ID);
    }

    @Test
    @DisplayName("Xoá khoản nợ đang hiệu lực trả về 400")
    void deletingActiveDebtReturns400() throws Exception {
        doThrow(new IllegalArgumentException(
                "Khoản nợ đang còn hiệu lực. Hãy đánh dấu 'Đã tất toán' trước khi xoá."))
                .when(debtService).deleteDebt(anyLong(), anyLong());

        mockMvc.perform(delete("/api/debts/{id}", DEBT_ID).principal(auth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.request").exists());
    }
}
