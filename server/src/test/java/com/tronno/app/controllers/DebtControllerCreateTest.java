package com.tronno.app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tronno.app.dtos.DebtDtos.DebtRequest;
import com.tronno.app.exception.GlobalExceptionHandler;
import com.tronno.app.service.DebtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;
import static com.tronno.app.controllers.DebtControllerResponseFactory.response;

/**
 * DebtControllerCreateTest
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
@DisplayName("DebtController POST /api/debts")
public class DebtControllerCreateTest {

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
    @DisplayName("Tạo khoản nợ hợp lệ trả về 200")
    void createsDebt() throws Exception {
        when(debtService.createDebt(eq(USER_ID), any(DebtRequest.class))).thenReturn(response());

        String body = """
                {
                  "name": "Vay mua xe",
                  "debtType": "BANK_LOAN",
                  "principal": 12000000,
                  "annualRate": 0,
                  "disbursementDate": "2026-01-01",
                  "termMonths": 12
                }
                """;

        mockMvc.perform(post("/api/debts").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DEBT_ID));

        ArgumentCaptor<DebtRequest> captor = ArgumentCaptor.forClass(DebtRequest.class);
        verify(debtService).createDebt(eq(USER_ID), captor.capture());
        assert captor.getValue().name().equals("Vay mua xe");
    }

    @Test
    @DisplayName("Thiếu tên khoản nợ trả về 400 kèm lỗi validation")
    void missingNameReturns400() throws Exception {
        String body = """
                {
                  "debtType": "BANK_LOAN",
                  "principal": 12000000,
                  "annualRate": 0,
                  "disbursementDate": "2026-01-01",
                  "termMonths": 12
                }
                """;

        mockMvc.perform(post("/api/debts").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dữ liệu không hợp lệ"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("Số tiền không tròn thì báo lỗi validation")
    void nonRoundAmountReturns400() throws Exception {
        String body = """
                {
                  "name": "Vay mua xe",
                  "debtType": "BANK_LOAN",
                  "principal": 1500,
                  "annualRate": 0,
                  "disbursementDate": "2026-01-01",
                  "termMonths": 12
                }
                """;

        mockMvc.perform(post("/api/debts").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.principal").exists());
    }

    @Test
    @DisplayName("Body không phải JSON trả về 400")
    void malformedBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/debts").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ khong phai json"))
                .andExpect(status().isBadRequest());
    }
}
