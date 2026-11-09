package com.tronno.app.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

/**
 * Gửi email nhắc nợ.
 * <p>Nếu chưa cấu hình SMTP ({@code app.mail.enabled=false}), nội dung nhắc hạn
 * chỉ được ghi log và hiển thị trên giao diện TRONNO (bảng {@code reminders}).
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

@Service
@RequiredArgsConstructor
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    /**
     * Kiểm tra đã bật gửi email qua SMTP hay chưa.
     *
     * @return boolean true nếu đã bật SMTP
     */
    public boolean isMailEnabled() {
        return mailEnabled;
    }

    /**
     * Gửi email nhắc trả nợ tới địa chỉ được chỉ định.
     *
     * @param toEmail địa chỉ nhận
     * @param subject tiêu đề email
     * @param body nội dung email
     * @return boolean true nếu gửi thành công, hoặc khi chưa bật mail
     * (khi đó nội dung vẫn xem được trên giao diện)
     */
    public boolean sendReminderMail(String toEmail, String subject, String body) {
        if (!mailEnabled) {
            log.info("[TRONNO] Chưa bật SMTP (mail.enabled=false). Nội dung nhắc hạn cho {}: {}",
                    toEmail, body);
            return true;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body == null ? "" : body);
            mailSender.send(message);
            log.info("[TRONNO] Đã gửi email nhắc hạn tới {}", toEmail);
            return true;
        } catch (Exception e) {
            log.warn("[TRONNO] Không gửi được email tới {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}
