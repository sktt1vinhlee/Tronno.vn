package com.tronno.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.models.DebtType;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleStatus;
import com.tronno.app.models.ScheduleType;
import com.tronno.app.models.TermType;

/**
 * DebtServiceTestData
 *
 * <p>Bộ dữ liệu dùng chung cho các test của {@link DebtService}.</p>
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
public final class DebtServiceTestData {

    /** Id người dùng sử dụng trong test. */
    public static final Long USER_ID = 1L;

    /** Id khoản nợ sử dụng trong test. */
    public static final Long DEBT_ID = 10L;

    /** Ngày dùng làm mốc ngày hiện tại trong test. */
    public static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    private DebtServiceTestData() {
    }

    /**
     * Tạo khoản nợ mẫu.
     *
     * @return khoản nợ mẫu
     */
    public static Debt debt() {
        return Debt.builder()
                .id(DEBT_ID)
                .userId(USER_ID)
                .name("Vay mua xe máy")
                .debtType(DebtType.BANK_LOAN)
                .principal(new BigDecimal("12000000"))
                .annualRate(BigDecimal.ZERO)
                .interestMethod(InterestMethod.REDUCING)
                .scheduleType(ScheduleType.EQUAL_PRINCIPAL)
                .disbursementDate(LocalDate.of(2026, 1, 1))
                .termMonths(12)
                .termType(TermType.FIXED_DATE)
                .status(DebtStatus.ACTIVE)
                .graceDays(0)
                .build();
    }

    /**
     * Tạo kỳ trả mẫu.
     *
     * @param id        mã kỳ trả
     * @param no        số thứ tự kỳ trả
     * @param dueDate   ngày đến hạn
     * @param principal gốc đến hạn
     * @return kỳ trả mẫu
     */
    public static RepaymentSchedule schedule(Long id, int no, LocalDate dueDate, String principal) {
        return RepaymentSchedule.builder()
                .id(id)
                .debtId(DEBT_ID)
                .installmentNo(no)
                .periodStart(dueDate.minusMonths(1))
                .periodEnd(dueDate)
                .dueDate(dueDate)
                .principalDue(new BigDecimal(principal))
                .interestDue(BigDecimal.ZERO)
                .feeDue(BigDecimal.ZERO)
                .totalDue(new BigDecimal(principal))
                .status(ScheduleStatus.PENDING)
                .build();
    }

    /**
     * Tạo builder cho request tạo khoản nợ.
     *
     * @param name       tên khoản nợ
     * @param principal  số tiền vay
     * @param termMonths số tháng kỳ hạn
     * @return builder request
     */
    public static DebtRequestBuilder request(String name, String principal, int termMonths) {
        return new DebtRequestBuilder()
                .name(name)
                .principal(new BigDecimal(principal))
                .termMonths(termMonths);
    }
}
