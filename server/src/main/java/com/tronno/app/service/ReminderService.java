package com.tronno.app.service;

import com.tronno.app.dtos.DebtDtos.ReminderResponse;
import com.tronno.app.exception.ResourceNotFoundException;
import com.tronno.app.models.*;
import com.tronno.app.repository.*;
import com.tronno.app.service.finance.Money;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Nhắc hạn TỰ ĐỘNG.
 * <p>Quy tắc: mỗi kỳ trả chưa được trả sẽ được nhắc mỗi ngày, bắt đầu từ
 * {@link #NOTIFY_BEFORE_DAYS} ngày trước ngày đến hạn, và tiếp tục nhắc mỗi ngày
 * cho tới khi khoản nợ được thanh toán (kỳ trả chuyển sang trạng thái PAID).
 * <p>Người dùng không cần đặt lịch nhắc thủ công nữa.
 */
@Service
@RequiredArgsConstructor
public class ReminderService {
    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    /** Số ngày trước hạn bắt đầu nhắc. */
    public static final int NOTIFY_BEFORE_DAYS = 5;

    private final ReminderRepository reminders;
    private final DebtRepository debts;
    private final RepaymentScheduleRepository schedules;
    private final UserRepository users;
    private final MailService mailService;

    @Transactional(readOnly = true)
    public List<ReminderResponse> list(Long userId) {
        return reminders.findByUserIdOrderByRemindAtAsc(userId).stream()
                .map(r -> {
                    Debt d = debts.findById(r.getDebtId()).orElse(null);
                    return toResponse(r, d);
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> listByDebt(Long userId, Long debtId) {
        debts.findByIdAndUserId(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản nợ"));
        return reminders.findByDebtIdOrderByRemindAtAsc(debtId).stream()
                .map(r -> {
                    Debt d = debts.findById(r.getDebtId()).orElse(null);
                    return toResponse(r, d);
                }).toList();
    }

    /**
     * Sinh & gửi nhắc hạn tự động cho tất cả khoản nợ đang mở.
     * <ul>
     *   <li>Kỳ trả còn tồn đọng và sắp tới hạn trong vòng {@link #NOTIFY_BEFORE_DAYS} ngày: nhắc trước.</li>
     *   <li>Kỳ trả đã quá hạn: nhắc mỗi ngày tới khi được trả.</li>
     * </ul>
     * Mỗi (kỳ trả, ngày) chỉ nhắc tối đa một lần.
     */
    @Transactional
    public int sendDueReminders(LocalDate today) {
        List<RepaymentSchedule> open = schedules.findOpenSchedulesDueBefore(today.plusDays(NOTIFY_BEFORE_DAYS));

        int sent = 0;
        for (RepaymentSchedule schedule : open) {
            Debt debt = debts.findById(schedule.getDebtId()).orElse(null);
            if (debt == null || debt.getStatus() != DebtStatus.ACTIVE) continue;

            User user = users.findById(debt.getUserId()).orElse(null);
            if (user == null || !user.isActive()) continue;

            if (reminders.existsByScheduleIdAndNotifyDate(schedule.getId(), today)) continue;

            ReminderType type = resolveType(schedule, today);
            if (type == null) continue;

            Reminder reminder = Reminder.builder()
                    .userId(debt.getUserId())
                    .debtId(debt.getId())
                    .scheduleId(schedule.getId())
                    .remindType(type)
                    .notifyDate(today)
                    .channel("EMAIL")
                    .remindAt(today)
                    .priority(priorityOf(debt))
                    .status(ReminderStatus.PENDING)
                    .message(buildMessage(debt, schedule, type, today))
                    .build();
            reminders.save(reminder);

            boolean ok = deliver(user, debt, reminder);
            reminder.setStatus(ok ? ReminderStatus.SENT : ReminderStatus.FAILED);
            if (ok) reminder.setSentAt(LocalDateTime.now());
            reminders.save(reminder);
            if (ok) sent++;
        }
        return sent;
    }

    /**
     * Gửi ngay một nhắc hạn (không chờ tới lịch 8:00).
     */
    @Transactional
    public ReminderResponse sendNow(Long userId, Long id) {
        Reminder r = reminders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhắc hạn"));
        Debt debt = debts.findByIdAndUserId(r.getDebtId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản nợ"));
        User user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        boolean ok = deliver(user, debt, r);
        r.setStatus(ok ? ReminderStatus.SENT : ReminderStatus.FAILED);
        if (ok) r.setSentAt(LocalDateTime.now());
        reminders.save(r);

        return toResponse(r, debt);
    }

    private ReminderType resolveType(RepaymentSchedule schedule, LocalDate today) {
        LocalDate due = schedule.getDueDate();
        if (due.isAfter(today)) return ReminderType.BEFORE_DUE;
        if (due.isEqual(today)) return ReminderType.ON_DUE;
        return ReminderType.OVERDUE;
    }

    private boolean deliver(User user, Debt debt, Reminder r) {
        String subject = "[TRONNO] Nhắc trả nợ: " + debt.getName();
        return mailService.sendReminderMail(user.getEmail(), subject, r.getMessage());
    }

    private int priorityOf(Debt debt) {
        int priority = 0;
        if (debt.getLateFeeType() != LateFeeType.NONE) {
            priority += 50;
            BigDecimal rate = debt.getLateFeeRate() == null ? BigDecimal.ZERO : debt.getLateFeeRate();
            if (rate.compareTo(BigDecimal.valueOf(0.03)) >= 0) priority += 30;
            else if (rate.compareTo(BigDecimal.valueOf(0.01)) > 0) priority += 15;
        }
        if (debt.getAnnualRate() != null) {
            if (debt.getAnnualRate().compareTo(BigDecimal.valueOf(20)) >= 0) priority += 40;
            else if (debt.getAnnualRate().compareTo(BigDecimal.valueOf(12)) >= 0) priority += 20;
        }
        if (debt.getDebtType() == DebtType.APP_LOAN) priority += 10;
        if (debt.getDebtType() == DebtType.CREDIT_CARD) priority += 5;
        return priority;
    }

    private String buildMessage(Debt debt, RepaymentSchedule schedule, ReminderType type, LocalDate today) {
        BigDecimal remaining = Money.subtract(schedule.getTotalDue(), schedule.getPaidAmount());
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, schedule.getDueDate());

        String prefix = switch (type) {
            case BEFORE_DUE -> String.format(
                    "Khoản nợ của bạn sắp tới hạn sau %d ngày (hạn ngày %s).", daysLeft, schedule.getDueDate());
            case ON_DUE -> "Khoản nợ của bạn đến hạn hôm nay.";
            case OVERDUE -> String.format(
                    "Khoản nợ của bạn đã trễ hạn %d ngày. Hệ thống sẽ nhắc bạn mỗi ngày "
                    + "cho tới khi khoản nợ được thanh toán.",
                    Math.abs(daysLeft));
            default -> "Nhắc trả nợ hằng ngày.";
        };

        return prefix + "\n\n"
                + "Khoản nợ: " + debt.getName() + "\n"
                + "Kỳ trả số " + schedule.getInstallmentNo() + "\n"
                + "Ngày đến hạn: " + schedule.getDueDate() + "\n"
                + "Số tiền còn phải trả: " + Money.round(remaining).toPlainString() + " đ\n\n"
                + (debt.getLateFeeType() != LateFeeType.NONE
                ? "Lưu ý: trễ hạn sẽ phát sinh phí phạt theo hợp đồng.\n" : "")
                + "Hãy đăng nhập TRONNO để xem chi tiết và ghi nhận thanh toán.";
    }

    private ReminderResponse toResponse(Reminder r, Debt d) {
        return new ReminderResponse(r.getId(), r.getDebtId(),
                d == null ? null : d.getName(), r.getScheduleId(), r.getRemindType(),
                r.getChannel(), r.getRemindAt(), r.getPriority(), r.getStatus(),
                r.getSentAt(), r.getMessage());
    }
}
