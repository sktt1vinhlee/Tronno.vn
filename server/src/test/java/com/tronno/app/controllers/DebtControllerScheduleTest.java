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
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tronno.app.dtos.DebtDtos.ScheduleResponse;
import com.tronno.app.exception.GlobalExceptionHandler;
import com.tronno.app.service.DebtService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;

/**
 * DebtControllerScheduleTest
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
@DisplayName("DebtController GET /api/debts/{id}/schedule")
public class DebtControllerScheduleTest {

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
    @DisplayName("Trả về lịch trả của khoản nợ")
    void returnsSchedule() throws Exception {
        ScheduleResponse s = new ScheduleResponse(1L, DEBT_ID, 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 1),
                new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal("1000000"), BigDecimal.ZERO, null, null);
        when(debtService.listSchedule(USER_ID, DEBT_ID)).thenReturn(List.of(s));

        mockMvc.perform(get("/api/debts/{id}/schedule", DEBT_ID).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].installmentNo").value(1))
                .andExpect(jsonPath("$[0].totalDue").value(1000000));
    }
}
