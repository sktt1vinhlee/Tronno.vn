package com.tronno.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.models.DebtType;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleStatus;

/**
 * LetterServiceTestData
 *
 * <p>Bộ dữ liệu dùng chung cho các test của {@link LetterService}.</p>
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
public final class LetterServiceTestData {

    private LetterServiceTestData() {
    }

    /**
     * Tạo khoản nợ mẫu dùng để soạn thư.
     *
     * @return khoản nợ mẫu
     */
    public static Debt debt() {
        return Debt.builder()
                .id(10L)
                .userId(1L)
                .name("Vay mua xe máy")
                .debtType(DebtType.FRIEND)
                .principal(new BigDecimal("5000000"))
                .annualRate(new BigDecimal("12"))
                .disbursementDate(LocalDate.of(2026, 1, 1))
                .termMonths(12)
                .status(DebtStatus.ACTIVE)
                .build();
    }

    /**
     * Tạo kỳ trả mẫu.
     *
     * @param dueDate  ngày đến hạn
     * @param totalDue tổng tiền đến hạn
     * @param paid     tiền đã trả
     * @return kỳ trả mẫu
     */
    public static RepaymentSchedule schedule(LocalDate dueDate, String totalDue, String paid) {
        return RepaymentSchedule.builder()
                .id(1L)
                .debtId(10L)
                .installmentNo(1)
                .dueDate(dueDate)
                .totalDue(new BigDecimal(totalDue))
                .paidAmount(new BigDecimal(paid))
                .status(ScheduleStatus.PENDING)
                .build();
    }
}
