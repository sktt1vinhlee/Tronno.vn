package com.tronno.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.models.DebtType;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleStatus;
import com.tronno.app.models.User;

/**
 * ReminderServiceTestData
 *
 * <p>Bộ dữ liệu dùng chung cho các test của {@link ReminderService}.</p>
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
public final class ReminderServiceTestData {

    /** Ngày dùng làm mốc ngày hiện tại trong test. */
    public static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    private ReminderServiceTestData() {
    }

    /**
     * Tạo khoản nợ mẫu.
     *
     * @return khoản nợ mẫu
     */
    public static Debt debt() {
        return Debt.builder()
                .id(10L)
                .userId(1L)
                .name("Vay mua xe")
                .debtType(DebtType.BANK_LOAN)
                .principal(new BigDecimal("12000000"))
                .annualRate(new BigDecimal("10"))
                .disbursementDate(LocalDate.of(2026, 1, 1))
                .termMonths(12)
                .status(DebtStatus.ACTIVE)
                .build();
    }

    /**
     * Tạo người dùng mẫu.
     *
     * @return người dùng mẫu
     */
    public static User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("vinh@example.com");
        user.setFullName("Hà Lê Vinh");
        user.setBlocked(false);
        return user;
    }

    /**
     * Tạo kỳ trả mẫu.
     *
     * @param id      mã kỳ trả
     * @param no      số thứ tự kỳ trả
     * @param dueDate ngày đến hạn
     * @return kỳ trả mẫu
     */
    public static RepaymentSchedule schedule(Long id, int no, LocalDate dueDate) {
        return RepaymentSchedule.builder()
                .id(id)
                .debtId(10L)
                .installmentNo(no)
                .dueDate(dueDate)
                .principalDue(new BigDecimal("1000000"))
                .interestDue(new BigDecimal("50000"))
                .feeDue(BigDecimal.ZERO)
                .totalDue(new BigDecimal("1050000"))
                .status(ScheduleStatus.PENDING)
                .build();
    }
}
