package com.tronno.app.config;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tronno.app.service.ReminderService;

/**
 * ReminderScheduler
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

@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReminderService reminderService;

    /**
     * Job chạy mỗi sáng 8:00 để gửi nhắc hạn.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        int sent = reminderService.sendDueReminders(LocalDate.now());
        if (sent > 0) log.info("TRONNO: đã gửi {} nhắc hạn", sent);
    }
}
