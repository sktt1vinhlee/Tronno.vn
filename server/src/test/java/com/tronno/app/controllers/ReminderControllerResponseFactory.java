package com.tronno.app.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tronno.app.dtos.DebtDtos.ReminderResponse;
import com.tronno.app.models.ReminderStatus;
import com.tronno.app.models.ReminderType;

/**
 * ReminderControllerResponseFactory
 *
 * <p>Tạo dữ liệu mẫu cho các test của {@link ReminderController}.</p>
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
public final class ReminderControllerResponseFactory {

    private ReminderControllerResponseFactory() {
    }

    /**
     * Tạo response nhắc hạn mẫu.
     *
     * @return response nhắc hạn mẫu
     */
    public static ReminderResponse response() {
        return new ReminderResponse(1L, ControllerTestData.DEBT_ID, "Vay mua xe", 5L,
                ReminderType.ON_DUE, "EMAIL", LocalDate.of(2026, 11, 10), 10,
                ReminderStatus.SENT, LocalDateTime.of(2026, 11, 10, 8, 0),
                "Nội dung nhắc trả nợ");
    }
}
