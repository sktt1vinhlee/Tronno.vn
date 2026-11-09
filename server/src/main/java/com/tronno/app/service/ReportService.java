package com.tronno.app.service;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.models.*;
import com.tronno.app.repository.*;
import com.tronno.app.service.finance.DateRules;
import com.tronno.app.service.finance.Money;
import com.tronno.app.service.finance.PaymentAllocator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final DebtRepository debts;
    private final RepaymentScheduleRepository schedules;
    private final PaymentRepository payments;
    private final UserRepository users;
    private final DebtService debtService;

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(Long userId) {
        LocalDate today = LocalDate.now();
        List<Debt> activeDebts = debts.findByUserIdAndStatusOrderByCreatedAtDesc(userId, DebtStatus.ACTIVE);
        List<DebtResponse> debtResponses = activeDebts.stream().map(debtService::toResponse).toList();

        List<RepaymentSchedule> allSchedules = new ArrayList<>();
        for (Debt d : activeDebts) {
            allSchedules.addAll(schedules.findByDebtIdOrderByInstallmentNoAsc(d.getId()));
        }

        BigDecimal totalRemaining = allSchedules.stream()
                .map(s -> Money.subtract(s.getTotalDue(), s.getPaidAmount()))
                .reduce(BigDecimal.ZERO, Money::add);
        BigDecimal totalPaid = allSchedules.stream()
                .map(RepaymentSchedule::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterestPaid = allSchedules.stream()
                .map(RepaymentSchedule::getInterestPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFeePaid = allSchedules.stream()
                .map(RepaymentSchedule::getFeePaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterestUpcoming = allSchedules.stream()
                .map(s -> Money.subtract(s.getInterestDue(), s.getInterestPaid()))
                .reduce(BigDecimal.ZERO, Money::add);

        BigDecimal totalPrincipal = activeDebts.stream()
                .map(Debt::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyPayment = activeDebts.stream()
                .map(d -> d.getScheduleType() == ScheduleType.ANNUITY
                        ? com.tronno.app.service.finance.ScheduleGenerator
                                .annuityPayment(d.getPrincipal(), d.getAnnualRate(), d.getTermMonths())
                        : Money.divide(d.getPrincipal(), BigDecimal.valueOf(Math.max(1, d.getTermMonths()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyIncome = users.findById(userId)
                .map(User::getMonthlyIncome)
                .orElse(BigDecimal.ZERO);
        BigDecimal dti = Money.isPositive(monthlyIncome)
                ? Money.divide(monthlyPayment, monthlyIncome).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        LocalDate debtFreeDate = allSchedules.stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .map(RepaymentSchedule::getDueDate)
                .max(Comparator.naturalOrder())
                .orElse(null);
        int daysToDebtFree = debtFreeDate == null ? 0
                : (int) DateRules.daysBetween(today, debtFreeDate);

        List<RepaymentSchedule> upcoming = allSchedules.stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .sorted(Comparator.comparing(RepaymentSchedule::getDueDate))
                .limit(8)
                .toList();
        List<ScheduleResponse> upcomingResponses = upcoming.stream()
                .map(s -> debtService.toScheduleResponse(s, findDebt(s.getDebtId()), today))
                .toList();

        RepaymentSchedule nextSchedule = upcoming.isEmpty() ? null : upcoming.get(0);
        BigDecimal nextDueAmount = nextSchedule == null ? Money.zero()
                : Money.subtract(nextSchedule.getTotalDue(), nextSchedule.getPaidAmount());

        List<CashflowMonth> cashflow = buildCashflow(allSchedules, today);

        List<PayoffPoint> payoffCurve = buildPayoffCurve(allSchedules, today);

        CashflowAlert alert = buildCashflowAlert(allSchedules, today, monthlyIncome);

        return new DashboardResponse(
                totalPrincipal, totalRemaining, totalPaid, totalInterestPaid,
                totalInterestUpcoming, totalFeePaid, activeDebts.size(),
                monthlyPayment, monthlyIncome, dti, debtFreeDate, daysToDebtFree,
                nextSchedule == null ? null : nextSchedule.getDueDate(), nextDueAmount,
                upcomingResponses, debtResponses, cashflow, payoffCurve, alert);
    }

    private Debt findDebt(Long debtId) {
        return debts.findById(debtId).orElse(null);
    }

    private List<CashflowMonth> buildCashflow(List<RepaymentSchedule> all, LocalDate today) {
        Map<YearMonth, BigDecimal> dueMap = new HashMap<>();
        Map<YearMonth, BigDecimal> paidMap = new HashMap<>();
        Map<YearMonth, BigDecimal> remainingMap = new HashMap<>();

        for (RepaymentSchedule s : all) {
            YearMonth ym = YearMonth.from(s.getDueDate());
            dueMap.merge(ym, s.getTotalDue(), BigDecimal::add);
            paidMap.merge(ym, s.getPaidAmount(), BigDecimal::add);
            remainingMap.merge(ym, Money.subtract(s.getTotalDue(), s.getPaidAmount()), BigDecimal::add);
        }

        List<CashflowMonth> result = new ArrayList<>();
        YearMonth start = YearMonth.from(today);
        for (int i = 0; i < 12; i++) {
            YearMonth ym = start.plusMonths(i);
            result.add(new CashflowMonth(ym.toString(),
                    dueMap.getOrDefault(ym, BigDecimal.ZERO),
                    paidMap.getOrDefault(ym, BigDecimal.ZERO),
                    remainingMap.getOrDefault(ym, BigDecimal.ZERO)));
        }
        return result;
    }

    private List<PayoffPoint> buildPayoffCurve(List<RepaymentSchedule> all, LocalDate today) {
        List<PayoffPoint> points = new ArrayList<>();

        BigDecimal currentRemaining = all.stream()
                .map(s -> Money.subtract(s.getTotalDue(), s.getPaidAmount()))
                .reduce(BigDecimal.ZERO, Money::add);
        points.add(new PayoffPoint(today.toString(), currentRemaining, BigDecimal.ZERO));

        List<RepaymentSchedule> sorted = all.stream()
                .sorted(Comparator.comparing(RepaymentSchedule::getDueDate))
                .toList();

        BigDecimal running = currentRemaining;
        BigDecimal runningInterest = BigDecimal.ZERO;
        for (RepaymentSchedule s : sorted) {
            running = Money.subtract(running, s.getTotalDue());
            runningInterest = Money.add(runningInterest, s.getInterestDue());
            points.add(new PayoffPoint(s.getDueDate().toString(), running, runningInterest));
        }
        return points;
    }

    private CashflowAlert buildCashflowAlert(List<RepaymentSchedule> all, LocalDate today,
            BigDecimal monthlyIncome) {
        LocalDate horizon = today.plusDays(7);
        List<RepaymentSchedule> week = all.stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .filter(s -> !s.getDueDate().isBefore(today) && !s.getDueDate().isAfter(horizon))
                .toList();

        BigDecimal total = week.stream()
                .map(s -> Money.subtract(s.getTotalDue(), s.getPaidAmount()))
                .reduce(BigDecimal.ZERO, Money::add);

        BigDecimal monthDue = all.stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .filter(s -> YearMonth.from(s.getDueDate()).equals(YearMonth.from(today)))
                .map(s -> Money.subtract(s.getTotalDue(), s.getPaidAmount()))
                .reduce(BigDecimal.ZERO, Money::add);
        BigDecimal projected = Money.subtract(monthlyIncome, monthDue);

        String level = "INFO";
        String message;
        if (week.isEmpty()) {
            message = "7 ngày tới không có khoản nợ nào đến hạn.";
        } else if (Money.isNegative(projected)) {
            level = "DANGER";
            message = String.format(
                    "Tuần tới có %d khoản đến hạn, tổng %s. Số dư dự kiến chỉ còn %s.",
                    week.size(), formatVnd(total), formatVnd(projected));
        } else {
            level = "WARNING";
            message = String.format(
                    "Tuần tới có %d khoản đến hạn, tổng %s. Số dư dự kiến còn %s.",
                    week.size(), formatVnd(total), formatVnd(projected));
        }

        return new CashflowAlert(week.size(), total, projected, message, level);
    }

    private String formatVnd(BigDecimal value) {
        return Money.round(value).toPlainString() + " đ";
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listAllPayments(Long userId) {
        List<PaymentResponse> result = new ArrayList<>();
        for (Debt d : debts.findByUserIdOrderByCreatedAtDesc(userId)) {
            result.addAll(debtService.listPayments(userId, d.getId()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<CashflowMonth> monthlyCashflow(Long userId, int months) {
        LocalDate today = LocalDate.now();
        List<RepaymentSchedule> all = new ArrayList<>();
        for (Debt d : debts.findByUserIdAndStatusOrderByCreatedAtDesc(userId, DebtStatus.ACTIVE)) {
            all.addAll(schedules.findByDebtIdOrderByInstallmentNoAsc(d.getId()));
        }
        return buildCashflow(all, today);
    }
}
