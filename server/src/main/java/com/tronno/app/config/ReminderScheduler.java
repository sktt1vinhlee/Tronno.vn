package com.tronno.app.config;

import com.tronno.app.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReminderService reminderService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        int sent = reminderService.sendDueReminders(LocalDate.now());
        if (sent > 0) log.info("TRONNO: đã gửi {} nhắc hạn", sent);
    }
}
