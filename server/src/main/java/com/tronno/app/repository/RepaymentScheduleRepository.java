package com.tronno.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleStatus;

/**
 * RepaymentScheduleRepository
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
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByDebtIdOrderByInstallmentNoAsc(Long debtId);

    List<RepaymentSchedule> findByDebtIdAndStatusInOrderByInstallmentNoAsc(
            Long debtId, List<ScheduleStatus> statuses);

    void deleteByDebtId(Long debtId);

    List<RepaymentSchedule> findByDueDateBetweenOrderByDueDateAsc(LocalDate from, LocalDate to);

    List<RepaymentSchedule> findByStatusOrderByDueDateAsc(ScheduleStatus status);

    /**
     * Các kỳ trả còn tồn đọng sắp tới hạn (trong horizon ngày) hoặc đã trễ hạn.
     * Dùng để sinh nhắc hạn tự động hằng ngày.
     */
    @Query("""
            select s from RepaymentSchedule s
            where s.status <> com.tronno.app.models.ScheduleStatus.PAID
              and s.dueDate <= :horizon
            order by s.dueDate asc
            """)
    List<RepaymentSchedule> findOpenSchedulesDueBefore(LocalDate horizon);
}
