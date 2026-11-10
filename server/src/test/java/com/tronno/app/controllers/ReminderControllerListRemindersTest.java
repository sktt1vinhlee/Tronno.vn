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
import com.tronno.app.service.ReminderService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.DEBT_ID;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;
import static com.tronno.app.controllers.ReminderControllerResponseFactory.response;

/**
 * ReminderControllerListRemindersTest
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
@DisplayName("ReminderController GET /api/reminders")
public class ReminderControllerListRemindersTest {

    @Mock
    private ReminderService reminderService;

    private MockMvc mockMvc;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReminderController(reminderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        auth = ControllerTestData.auth();
    }

    @Test
    @DisplayName("Trả về danh sách nhắc hạn của người dùng")
    void returnsList() throws Exception {
        when(reminderService.list(USER_ID)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/reminders").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].debtName").value("Vay mua xe"))
                .andExpect(jsonPath("$[0].remindType").value("ON_DUE"))
                .andExpect(jsonPath("$[0].status").value("SENT"));
    }

    @Test
    @DisplayName("Trả về nhắc hạn của một khoản nợ")
    void returnsRemindersForDebt() throws Exception {
        when(reminderService.listByDebt(USER_ID, DEBT_ID)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/debts/{debtId}/reminders", DEBT_ID).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].debtId").value(DEBT_ID));
    }
}
