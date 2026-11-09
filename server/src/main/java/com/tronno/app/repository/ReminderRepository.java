package com.tronno.app.repository;

import com.tronno.app.models.Reminder;
import com.tronno.app.models.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);
    List<Reminder> findByDebtIdOrderByRemindAtAsc(Long debtId);
    List<Reminder> findByStatusAndRemindAtLessThanEqualOrderByPriorityDesc(ReminderStatus status, LocalDate date);
    long countByUserId(Long userId);
    void deleteByDebtId(Long debtId);

    /** Kiểm tra kỳ trả này đã được nhắc trong ngày d chưa (chống gửi trùng). */
    boolean existsByScheduleIdAndNotifyDate(Long scheduleId, LocalDate notifyDate);
}
