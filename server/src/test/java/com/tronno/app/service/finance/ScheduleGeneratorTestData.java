package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.models.DebtType;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.ScheduleType;
import com.tronno.app.models.TermType;

/**
 * ScheduleGeneratorTestData
 *
 * <p>Bộ dữ liệu dùng chung cho các test của {@link ScheduleGenerator}.</p>
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
public final class ScheduleGeneratorTestData {

    /** Ngày giải ngân dùng làm mốc trong test. */
    public static final LocalDate DISBURSE = LocalDate.of(2026, 1, 1);

    private ScheduleGeneratorTestData() {
    }

    /**
     * Tạo khoản nợ mẫu.
     *
     * @param scheduleType kiểu lịch trả
     * @param method       phương pháp tính lãi
     * @param termMonths   số tháng kỳ hạn
     * @return khoản nợ mẫu
     */
    public static Debt debt(ScheduleType scheduleType, InterestMethod method, int termMonths) {
        return Debt.builder()
                .id(1L)
                .userId(1L)
                .name("Khoản nợ test")
                .debtType(DebtType.BANK_LOAN)
                .principal(new BigDecimal("12000000"))
                .annualRate(BigDecimal.ZERO)
                .interestMethod(method)
                .scheduleType(scheduleType)
                .disbursementDate(DISBURSE)
                .termMonths(termMonths)
                .termType(TermType.FIXED_DATE)
                .status(DebtStatus.ACTIVE)
                .build();
    }

    /**
     * Sinh lịch trả từ khoản nợ mẫu.
     *
     * @param debt          khoản nợ
     * @param paidPrincipal gốc đã trả
     * @param paidAmount    tổng tiền đã trả
     * @return danh sách kỳ trả
     */
    public static List<com.tronno.app.models.RepaymentSchedule> generate(
            Debt debt, BigDecimal paidPrincipal, BigDecimal paidAmount) {
        return ScheduleGenerator.generate(debt, List.of(), paidPrincipal, paidAmount);
    }
}
