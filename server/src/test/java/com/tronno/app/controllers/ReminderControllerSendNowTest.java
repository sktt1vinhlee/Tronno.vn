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
import com.tronno.app.service.ReminderService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tronno.app.controllers.ControllerTestData.USER_ID;
import static com.tronno.app.controllers.ReminderControllerResponseFactory.response;

/**
 * ReminderControllerSendNowTest
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
@DisplayName("ReminderController POST /api/reminders/{id}/send")
class ReminderControllerSendNowTest {

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
    @DisplayName("Gửi ngay nhắc hạn thành công")
    void sendsReminder() throws Exception {
        when(reminderService.sendNow(USER_ID, 1L)).thenReturn(response());

        mockMvc.perform(post("/api/reminders/{id}/send", 1L).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(reminderService).sendNow(USER_ID, 1L);
    }
}
