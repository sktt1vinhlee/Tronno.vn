package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.tronno.app.models.Debt;
import com.tronno.app.models.RepaymentSchedule;

/**
 * PaymentAllocator
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
public final class PaymentAllocator {
    public static final String COMPONENT_LATE_FEE = "LATE_FEE";
    public static final String COMPONENT_FEE = "FEE";
    public static final String COMPONENT_INTEREST = "INTEREST";
    public static final String COMPONENT_PRINCIPAL = "PRINCIPAL";

    private PaymentAllocator() {
    }

    public record Allocation(Long scheduleId, String component, BigDecimal amount) {
    }

    public record Result(List<Allocation> allocations, BigDecimal applied, BigDecimal surplus) {
    }

    /**
     * Phân bổ một khoản tiền trả vào các kỳ trả theo thứ tự ưu tiên phí phạt trễ, phí, lãi rồi gốc.
     *
     * @param amount số tiền trả
     * @param schedules danh sách kỳ trả
     * @param debt khoản nợ
     * @param today ngày hiện tại
     * @param targetScheduleId id kỳ trả được chỉ định, có thể null
     * @return Result chứa các khoản phân bổ, tổng đã áp dụng và tiền dư
     */
    public static Result allocate(
            BigDecimal amount,
            List<RepaymentSchedule> schedules,
            Debt debt,
            LocalDate today,
            Long targetScheduleId) {
        BigDecimal remaining = Money.of(amount);
        BigDecimal applied = Money.zero();
        java.util.List<Allocation> allocations = new java.util.ArrayList<>();

        for (RepaymentSchedule schedule : schedules) {
            if (Money.compareTo(remaining, Money.zero()) <= 0) break;

            BigDecimal feeOutstanding = Money.subtract(schedule.getFeeDue(), schedule.getFeePaid());
            BigDecimal interestOutstanding = Money.subtract(schedule.getInterestDue(), schedule.getInterestPaid());
            BigDecimal principalOutstanding = Money.subtract(schedule.getPrincipalDue(), schedule.getPrincipalPaid());

            if (targetScheduleId == null && !schedule.getDueDate().isBefore(today)
                    && schedule.getDueDate().isAfter(today.plusDays(30))) {
                continue;
            }
            if (targetScheduleId != null && !targetScheduleId.equals(schedule.getId())) {
                continue;
            }

            if (debt != null && schedule.getDueDate().isBefore(today)) {
                long overdueDays = DateRules.daysBetween(schedule.getDueDate(), today)
                        - (debt.getGraceDays() == null ? 0 : debt.getGraceDays());
                if (overdueDays > 0) {
                    BigDecimal base = Money.add(
                            Money.subtract(schedule.getPrincipalDue(), schedule.getPrincipalPaid()),
                            Money.subtract(schedule.getInterestDue(), schedule.getInterestPaid()));
                    BigDecimal extraFee = ScheduleGenerator.lateFee(debt, overdueDays, Money.max(base, Money.zero()));
                    if (Money.isPositive(extraFee)) {
                        BigDecimal take = Money.min(remaining, extraFee);
                        allocations.add(new Allocation(schedule.getId(), COMPONENT_LATE_FEE, take));
                        remaining = Money.subtract(remaining, take);
                        applied = Money.add(applied, take);
                    }
                }
            }

            if (Money.compareTo(remaining, Money.zero()) <= 0) break;
            if (Money.isPositive(feeOutstanding)) {
                BigDecimal take = Money.min(remaining, feeOutstanding);
                allocations.add(new Allocation(schedule.getId(), COMPONENT_FEE, take));
                remaining = Money.subtract(remaining, take);
                applied = Money.add(applied, take);
            }

            if (Money.compareTo(remaining, Money.zero()) <= 0) break;
            if (Money.isPositive(interestOutstanding)) {
                BigDecimal take = Money.min(remaining, interestOutstanding);
                allocations.add(new Allocation(schedule.getId(), COMPONENT_INTEREST, take));
                remaining = Money.subtract(remaining, take);
                applied = Money.add(applied, take);
            }

            if (Money.compareTo(remaining, Money.zero()) <= 0) break;
            if (Money.isPositive(principalOutstanding)) {
                BigDecimal take = Money.min(remaining, principalOutstanding);
                allocations.add(new Allocation(schedule.getId(), COMPONENT_PRINCIPAL, take));
                remaining = Money.subtract(remaining, take);
                applied = Money.add(applied, take);
            }
        }

        BigDecimal surplus = Money.max(remaining, Money.zero());
        return new Result(allocations, applied, surplus);
    }

    /**
     * Tính phí phạt trễ hạn đang tồn tại tại một kỳ trả.
     *
     * @param debt khoản nợ
     * @param schedule kỳ trả
     * @param today ngày hiện tại
     * @return BigDecimal tiền phạt trễ hạn
     */
    public static BigDecimal currentLateFee(Debt debt, RepaymentSchedule schedule, LocalDate today) {
        if (debt == null || schedule == null) return Money.zero();
        if (!schedule.getDueDate().isBefore(today)) return Money.zero();
        long overdueDays = DateRules.daysBetween(schedule.getDueDate(), today)
                - (debt.getGraceDays() == null ? 0 : debt.getGraceDays());
        if (overdueDays <= 0) return Money.zero();
        BigDecimal base = Money.add(
                Money.subtract(schedule.getPrincipalDue(), schedule.getPrincipalPaid()),
                Money.subtract(schedule.getInterestDue(), schedule.getInterestPaid()));
        return ScheduleGenerator.lateFee(debt, overdueDays, Money.max(base, Money.zero()));
    }
}
