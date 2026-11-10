package com.tronno.app.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

import com.tronno.app.dtos.DebtDtos.PaymentRequest;
import com.tronno.app.dtos.DebtDtos.PaymentResponse;
import com.tronno.app.exception.GlobalExceptionHandler;
import com.tronno.app.service.DebtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;

/**
 * DebtControllerPayTest
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
@DisplayName("DebtController POST /api/debts/{id}/payments")
public class DebtControllerPayTest {

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
    @DisplayName("Ghi nhận thanh toán thành công")
    void recordsPayment() throws Exception {
        PaymentResponse res = new PaymentResponse(99L, DEBT_ID, "Vay mua xe",
                new BigDecimal("500000"), new BigDecimal("500000"), BigDecimal.ZERO,
                LocalDate.of(2026, 11, 10), "CASH", null, "RECORDED", List.of());
        when(debtService.recordPayment(eq(USER_ID), eq(DEBT_ID), any(PaymentRequest.class)))
                .thenReturn(res);

        String body = """
                {
                  "amount": 500000,
                  "paidAt": "2026-11-10",
                  "method": "CASH"
                }
                """;

        mockMvc.perform(post("/api/debts/{id}/payments", DEBT_ID).principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.amount").value(500000));
    }

    @Test
    @DisplayName("Số tiền thanh toán bằng 0 bị từ chối")
    void zeroAmountReturns400() throws Exception {
        String body = """
                {
                  "amount": 0,
                  "paidAt": "2026-11-10"
                }
                """;

        mockMvc.perform(post("/api/debts/{id}/payments", DEBT_ID).principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }
}
