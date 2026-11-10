package com.tronno.app.service.finance;

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
 * PaymentAllocatorTestData
 *
 * <p>Bộ dữ liệu dùng chung cho các test của {@link PaymentAllocator}.</p>
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
public final class PaymentAllocatorTestData {

    /** Ngày dùng làm mốc ngày hiện tại trong test. */
    public static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    private PaymentAllocatorTestData() {
    }

    /**
     * Tạo khoản nợ mẫu với lãi suất 0 và lịch trả gốc đều.
     *
     * @return khoản nợ mẫu
     */
    public static Debt debt() {
        return Debt.builder()
                .id(1L)
                .userId(1L)
                .name("Khoản nợ test")
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
     * @param id       mã kỳ trả
     * @param no       số thứ tự kỳ trả
     * @param dueDate  ngày đến hạn
     * @param principal gốc đến hạn
     * @param interest lãi đến hạn
     * @param fee      phí đến hạn
     * @return kỳ trả mẫu
     */
    public static RepaymentSchedule schedule(Long id, int no, LocalDate dueDate,
                                            String principal, String interest, String fee) {
        return RepaymentSchedule.builder()
                .id(id)
                .debtId(1L)
                .installmentNo(no)
                .periodStart(dueDate.minusMonths(1))
                .periodEnd(dueDate)
                .dueDate(dueDate)
                .principalDue(new BigDecimal(principal))
                .interestDue(new BigDecimal(interest))
                .feeDue(new BigDecimal(fee))
                .totalDue(new BigDecimal(principal).add(new BigDecimal(interest))
                        .add(new BigDecimal(fee)))
                .status(ScheduleStatus.PENDING)
                .build();
    }
}
