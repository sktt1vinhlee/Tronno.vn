package com.tronno.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;

/**
 * Reminder
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
@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "debt_id", nullable = false)
    private Long debtId;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "remind_type", nullable = false, length = 24)
    private ReminderType remindType;

    /** Ngày đã nhắc - dùng để chống gửi trùng cùng một kỳ trả trong cùng một ngày. */
    @Column(name = "notify_date")
    private java.time.LocalDate notifyDate;

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "remind_at", nullable = false)
    private java.time.LocalDate remindAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
