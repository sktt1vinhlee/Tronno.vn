package com.tronno.app.service.finance;

import com.tronno.app.models.Debt;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.InterestRateHistory;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.ScheduleStatus;
import com.tronno.app.models.ScheduleType;
import com.tronno.app.models.TermType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ScheduleGenerator {
    public static final BigDecimal DAYS_PER_YEAR = BigDecimal.valueOf(365);

    private ScheduleGenerator() {
    }

    public static List<RepaymentSchedule> generate(
            Debt debt,
            List<InterestRateHistory> rateHistory,
            BigDecimal paidPrincipal,
            BigDecimal paidAmount) {
        List<RepaymentSchedule> result = new ArrayList<>();
        // Kỳ trả theo quý: mỗi kỳ 3 tháng một lần.
        boolean quarterly = debt.getTermType() == TermType.QUARTERLY;
        int stepMonths = quarterly ? 3 : 1;
        int totalMonths = Math.max(stepMonths, debt.getTermMonths());
        int term = Math.max(1, totalMonths / stepMonths);
        BigDecimal principal = Money.of(debt.getPrincipal());
        BigDecimal outstanding = Money.subtract(principal, Money.of(paidPrincipal));
        if (Money.isNegative(outstanding)) outstanding = Money.zero();

        BigDecimal paidSoFar = Money.of(paidAmount);
        BigDecimal carry = Money.zero();

        BigDecimal annuityPayment = debt.getScheduleType() == ScheduleType.ANNUITY
                ? annuityPayment(principal, debt.getAnnualRate(), term)
                : null;

        for (int i = 1; i <= term; i++) {
            LocalDate periodStart = DateRules.addMonths(debt.getDisbursementDate(), (i - 1) * stepMonths);
            LocalDate periodEnd = DateRules.addMonths(debt.getDisbursementDate(), i * stepMonths);
            LocalDate dueDate = DateRules.adjustToBusinessDay(periodEnd);
            long days = DateRules.daysInclusive(periodStart, periodEnd);

            BigDecimal rate = rateFor(rateHistory, periodStart, debt.getAnnualRate());

            BigDecimal principalDue;
            switch (debt.getScheduleType()) {
                case INTEREST_ONLY -> principalDue = (i == term) ? outstanding : Money.zero();
                case ANNUITY -> principalDue = Money.zero();
                default -> {
                    BigDecimal evenPart = Money.divide(outstanding, BigDecimal.valueOf(term - i + 1));
                    principalDue = Money.min(evenPart, outstanding);
                }
            }

            BigDecimal interestDue = interestFor(debt, outstanding, rate, days, periodStart);

            if (annuityPayment != null) {
                BigDecimal principalPart = Money.subtract(annuityPayment, interestDue);
                if (Money.isNegative(principalPart)) principalPart = Money.zero();
                if (i == term) {
                    principalPart = outstanding;
                }
                principalDue = Money.min(principalPart, outstanding);
            }

            BigDecimal totalDue = Money.add(Money.add(principalDue, interestDue), carry);
            carry = Money.zero();

            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .debtId(debt.getId())
                    .installmentNo(i)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .dueDate(dueDate)
                    .principalDue(principalDue)
                    .interestDue(interestDue)
                    .feeDue(Money.zero())
                    .totalDue(totalDue)
                    .principalPaid(Money.zero())
                    .interestPaid(Money.zero())
                    .feePaid(Money.zero())
                    .paidAmount(Money.zero())
                    .status(ScheduleStatus.PENDING)
                    .build();

            BigDecimal remainingPaid = paidSoFar;
            if (Money.isPositive(remainingPaid)) {
                BigDecimal toFee = Money.min(remainingPaid, schedule.getFeeDue());
                remainingPaid = Money.subtract(remainingPaid, toFee);
                schedule.setFeePaid(toFee);

                BigDecimal toInterest = Money.min(remainingPaid, schedule.getInterestDue());
                remainingPaid = Money.subtract(remainingPaid, toInterest);
                schedule.setInterestPaid(toInterest);

                BigDecimal toPrincipal = Money.min(remainingPaid, schedule.getPrincipalDue());
                remainingPaid = Money.subtract(remainingPaid, toPrincipal);
                schedule.setPrincipalPaid(toPrincipal);

                BigDecimal applied = Money.add(Money.add(toFee, toInterest), toPrincipal);
                schedule.setPaidAmount(applied);
                if (Money.isZero(applied)) {
                    schedule.setStatus(ScheduleStatus.PENDING);
                } else if (Money.compareTo(applied, schedule.getTotalDue()) < 0) {
                    schedule.setStatus(ScheduleStatus.PARTIAL);
                } else {
                    schedule.setStatus(ScheduleStatus.PAID);
                    schedule.setPaidAt(dueDate);
                }
            }

            paidSoFar = Money.subtract(paidSoFar, schedule.getPaidAmount());
            outstanding = Money.subtract(outstanding, principalDue);

            if (debt.getInterestMethod() == InterestMethod.COMPOUND) {
                BigDecimal unpaidInterest = Money.subtract(interestDue, schedule.getInterestPaid());
                if (Money.isPositive(unpaidInterest)) {
                    outstanding = Money.add(outstanding, unpaidInterest);
                }
            }

            result.add(schedule);
        }

        return result;
    }

    private static BigDecimal interestFor(
            Debt debt, BigDecimal outstanding, BigDecimal annualRate, long days, LocalDate periodStart) {
        if (Money.isZero(outstanding) || annualRate == null || annualRate.signum() == 0) {
            return Money.zero();
        }
        BigDecimal daysFactor = BigDecimal.valueOf(days);
        BigDecimal dailyRate = annualRate.divide(BigDecimal.valueOf(100), 12, java.math.RoundingMode.HALF_UP)
                .divide(DAYS_PER_YEAR, 12, java.math.RoundingMode.HALF_UP);

        BigDecimal base = switch (debt.getInterestMethod()) {
            case SIMPLE -> Money.of(debt.getPrincipal());
            default -> outstanding;
        };

        return Money.multiply(Money.multiply(base, dailyRate), daysFactor);
    }

    public static BigDecimal rateFor(
            List<InterestRateHistory> histories, LocalDate date, BigDecimal fallback) {
        if (histories == null || histories.isEmpty()) return fallback;
        BigDecimal result = fallback;
        LocalDate bestFrom = null;
        for (InterestRateHistory h : histories) {
            if (h.getEffectiveFrom() == null || h.getEffectiveFrom().isAfter(date)) continue;
            if (h.getEffectiveTo() != null && h.getEffectiveTo().isBefore(date)) continue;
            if (bestFrom == null || !h.getEffectiveFrom().isBefore(bestFrom)) {
                bestFrom = h.getEffectiveFrom();
                result = h.getAnnualRate();
            }
        }
        return result;
    }

    public static BigDecimal annuityPayment(BigDecimal principal, BigDecimal annualRate, int term) {
        if (principal == null || principal.signum() == 0) return Money.zero();
        BigDecimal r = annualRate == null ? BigDecimal.ZERO
                : annualRate.divide(BigDecimal.valueOf(100), 12, java.math.RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(12), 12, java.math.RoundingMode.HALF_UP);
        if (r.signum() == 0) return Money.divide(principal, BigDecimal.valueOf(term));
        BigDecimal onePlusR = BigDecimal.ONE.add(r);
        BigDecimal factor = onePlusR.pow(term);
        BigDecimal denominator = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(factor, 16, java.math.RoundingMode.HALF_UP));
        if (denominator.signum() == 0) return Money.divide(principal, BigDecimal.valueOf(term));
        return Money.multiply(principal, r).divide(denominator, 6, java.math.RoundingMode.HALF_UP);
    }

    public static BigDecimal lateFee(Debt debt, long overdueDays, BigDecimal balance) {
        if (overdueDays <= 0) return Money.zero();
        return switch (debt.getLateFeeType()) {
            case PERCENT_PER_DAY -> {
                BigDecimal rate = Money.roundRate(debt.getLateFeeRate());
                yield Money.multiply(Money.multiply(balance, rate.divide(BigDecimal.valueOf(100), 12,
                        java.math.RoundingMode.HALF_UP)), BigDecimal.valueOf(overdueDays));
            }
            case FIXED_PER_DAY -> Money.multiply(
                    Money.of(debt.getLateFeeAmount()), BigDecimal.valueOf(overdueDays));
            default -> Money.zero();
        };
    }
}
