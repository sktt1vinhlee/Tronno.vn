package com.tronno.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tronno.app.models.Reminder;
import com.tronno.app.models.ReminderStatus;

/**
 * ReminderRepository
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
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);
    List<Reminder> findByDebtIdOrderByRemindAtAsc(Long debtId);
    List<Reminder> findByStatusAndRemindAtLessThanEqualOrderByPriorityDesc(ReminderStatus status, LocalDate date);
    long countByUserId(Long userId);
    void deleteByDebtId(Long debtId);

    /** Kiểm tra kỳ trả này đã được nhắc trong ngày d chưa (chống gửi trùng). */
    boolean existsByScheduleIdAndNotifyDate(Long scheduleId, LocalDate notifyDate);
}
