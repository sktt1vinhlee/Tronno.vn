package com.tronno.app.controllers;

import java.time.LocalDate;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tronno.app.service.ReminderService;

/**
 * Chạy nhắc hạn tự động ngay lập tức (ngoài lịch 8:00 mỗi sáng).
 * Dùng để kiểm thử hoặc khi cần gửi nhắc nợ ngay lập tức.
 *
 * Version 1.0
 *
 * Date: 09-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 09-11-2026         VinhHL             Create
 */

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderTriggerController {
    private final ReminderService reminderService;

    /**
     * Chạy thủ công job sinh nhắc hạn.
     *
     * @param a đối tượng xác thực
     * @return Map kết quả chạy job
     */
    @PostMapping("/run")
    public Map<String, Object> run(Authentication a) {
        int sent = reminderService.sendDueReminders(LocalDate.now());
        return Map.of("sent", sent, "date", LocalDate.now().toString());
    }
}
