package com.tronno.app.controllers;

import com.tronno.app.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * Chạy nhắc hạn tự động ngay lập tức (ngoài lịch 8:00 mỗi sáng).
 * Dùng để kiểm thử hoặc khi cần gửi nhắc nợ ngay lập tức.
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderTriggerController {
    private final ReminderService reminderService;

    @PostMapping("/run")
    public Map<String, Object> run(Authentication a) {
        int sent = reminderService.sendDueReminders(LocalDate.now());
        return Map.of("sent", sent, "date", LocalDate.now().toString());
    }
}
