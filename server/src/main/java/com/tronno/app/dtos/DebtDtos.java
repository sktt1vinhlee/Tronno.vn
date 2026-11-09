package com.tronno.app.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tronno.app.models.*;
import com.tronno.app.validation.RoundAmount;

/**
 * DebtDtos
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
public class DebtDtos {
    public record CreditorRequest(
            @NotBlank(message = "Vui lòng nhập tên chủ nợ")
            @Size(max = 150, message = "Tên chủ nợ tối đa 150 ký tự")
            String name,
            CreditorKind kind,
            @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
            String phone,
            @Size(max = 190, message = "Email tối đa 190 ký tự")
            String email,
            @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
            String note) {
    }

    public record CreditorResponse(
            Long id, String name, CreditorKind kind, String phone, String email, String note) {
        public static CreditorResponse from(Creditor c) {
            return new CreditorResponse(c.getId(), c.getName(), c.getKind(), c.getPhone(),
                    c.getEmail(), c.getNote());
        }
    }

    public record DebtRequest(
            @NotBlank(message = "Vui lòng nhập tên khoản nợ")
            @Size(max = 150, message = "Tên khoản nợ tối đa 150 ký tự")
            String name,
            @NotNull(message = "Vui lòng chọn loại nợ")
            DebtType debtType,
            Long creditorId,
            @NotNull(message = "Vui lòng nhập số tiền vay")
            @DecimalMin(value = "1000.00", message = "Số tiền vay tối thiểu 1.000 đồng")
            @RoundAmount(message = "Số tiền vay phải là số tròn (chia hết cho 1000) và tối thiểu 1.000 đồng")
            BigDecimal principal,
            @NotNull(message = "Vui lòng nhập lãi suất")
            @DecimalMin(value = "0.00", message = "Lãi suất không được âm")
            @DecimalMax(value = "1000.00", message = "Lãi suất không hợp lý")
            BigDecimal annualRate,
            InterestMethod interestMethod,
            ScheduleType scheduleType,
            @NotNull(message = "Vui lòng chọn ngày giải ngân")
            @PastOrPresent(message = "Ngày giải ngân không được ở tương lai")
            LocalDate disbursementDate,
            @NotNull(message = "Vui lòng nhập kỳ hạn")
            @Min(value = 1, message = "Kỳ hạn tối thiểu 1 tháng")
            @Max(value = 480, message = "Kỳ hạn tối đa 480 tháng")
            Integer termMonths,
            // Kiểu thời hạn: FIXED_DATE (chọn ngày đáo hạn) hoặc QUARTERLY (trả theo quý).
            TermType termType,
            // Ngày đáo hạn cụ thể (bắt buộc khi termType = FIXED_DATE).
            LocalDate maturityDate,
            @DecimalMin(value = "0.00", message = "Phí trễ không được âm")
            BigDecimal lateFeeRate,
            @DecimalMin(value = "0.00", message = "Phí trễ cố định không được âm")
            BigDecimal lateFeeAmount,
            LateFeeType lateFeeType,
            @DecimalMin(value = "0.00", message = "Phí tất toán sớm không được âm")
            BigDecimal earlyFeeRate,
            EarlyFeeType earlyFeeType,
            @DecimalMin(value = "0.00", message = "Phí tất toán sớm không được âm")
            BigDecimal earlyFeeAmount,
            @Min(value = 0, message = "Số ngày ân hạn không được âm")
            Integer graceDays,
            @Size(max = 2000, message = "Ghi chú tối đa 2000 ký tự")
            String note,
            @Size(max = 1000, message = "Link hợp đồng tối đa 1000 ký tự")
            String contractUrl,
            // Chủ nợ nhập tay ngay trong form (dùng khi không chọn chủ nợ có sẵn).
            @Size(max = 150, message = "Tên chủ nợ tối đa 150 ký tự")
            String creditorName,
            CreditorKind creditorKind,
            @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
            String creditorPhone,
            @Size(max = 190, message = "Email tối đa 190 ký tự")
            String creditorEmail) {
    }

    public record DebtResponse(
            Long id,
            String name,
            DebtType debtType,
            Long creditorId,
            String creditorName,
            BigDecimal principal,
            BigDecimal annualRate,
            InterestMethod interestMethod,
            ScheduleType scheduleType,
            LocalDate disbursementDate,
            Integer termMonths,
            TermType termType,
            LocalDate maturityDate,
            BigDecimal installmentAmount,
            LateFeeType lateFeeType,
            BigDecimal lateFeeRate,
            BigDecimal lateFeeAmount,
            EarlyFeeType earlyFeeType,
            BigDecimal earlyFeeRate,
            BigDecimal earlyFeeAmount,
            Integer graceDays,
            DebtStatus status,
            String note,
            String contractUrl,
            BigDecimal remainingPrincipal,
            BigDecimal totalPaid,
            BigDecimal totalInterestPaid,
            BigDecimal totalFeePaid,
            LocalDate nextDueDate,
            BigDecimal nextDueAmount,
            LocalDate debtFreeDate) {
    }

    public record ScheduleResponse(
            Long id,
            Long debtId,
            Integer installmentNo,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate dueDate,
            BigDecimal principalDue,
            BigDecimal interestDue,
            BigDecimal feeDue,
            BigDecimal totalDue,
            BigDecimal principalPaid,
            BigDecimal interestPaid,
            BigDecimal feePaid,
            BigDecimal paidAmount,
            BigDecimal remaining,
            BigDecimal currentLateFee,
            ScheduleStatus status,
            LocalDate paidAt) {
    }

    public record PaymentRequest(
            @NotNull(message = "Vui lòng nhập số tiền thanh toán")
            @DecimalMin(value = "1000.00", message = "Số tiền thanh toán tối thiểu 1.000 đồng")
            @RoundAmount(message = "Số tiền thanh toán phải là số tròn (chia hết cho 1000) và tối thiểu 1.000 đồng")
            BigDecimal amount,
            @NotNull(message = "Vui lòng chọn ngày thanh toán")
            LocalDate paidAt,
            @Size(max = 32) String method,
            @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
            String note,
            Long scheduleId) {
    }

    public record PaymentResponse(
            Long id,
            Long debtId,
            String debtName,
            BigDecimal amount,
            BigDecimal appliedAmount,
            BigDecimal surplus,
            LocalDate paidAt,
            String method,
            String note,
            String status,
            List<AllocationResponse> allocations) {
    }

    public record AllocationResponse(
            Long scheduleId,
            Integer installmentNo,
            LocalDate dueDate,
            String component,
            BigDecimal amount) {
    }

    public record RateChangeRequest(
            @NotNull(message = "Vui lòng nhập lãi suất mới")
            @DecimalMin(value = "0.00", message = "Lãi suất không được âm")
            BigDecimal annualRate,
            @NotNull(message = "Vui lòng chọn ngày áp dụng")
            LocalDate effectiveFrom,
            @Size(max = 500) String note) {
    }

    public record RateChangeResponse(
            Long id, BigDecimal annualRate, LocalDate effectiveFrom, LocalDate effectiveTo, String note) {
    }

    public record ExtensionRequest(
            @NotNull(message = "Vui lòng nhập số tháng gia hạn")
            @Min(value = 1, message = "Số tháng gia hạn tối thiểu 1")
            @Max(value = 120, message = "Số tháng gia hạn tối đa 120")
            Integer extraMonths,
            BigDecimal newAnnualRate,
            @Size(max = 500) String note) {
    }

    public record ReversalRequest(
            @Size(max = 500, message = "Lý do tối đa 500 ký tự")
            String reason) {
    }

    public record DashboardResponse(
            BigDecimal totalDebt,
            BigDecimal totalRemaining,
            BigDecimal totalPaid,
            BigDecimal totalInterestPaid,
            BigDecimal totalInterestUpcoming,
            BigDecimal totalFeePaid,
            int activeDebtCount,
            BigDecimal monthlyPayment,
            BigDecimal monthlyIncome,
            BigDecimal dti,
            LocalDate debtFreeDate,
            int daysToDebtFree,
            LocalDate nextDueDate,
            BigDecimal nextDueAmount,
            List<ScheduleResponse> upcomingSchedules,
            List<DebtResponse> debts,
            List<CashflowMonth> cashflow,
            List<PayoffPoint> payoffCurve,
            CashflowAlert cashflowAlert) {
    }

    public record CashflowMonth(String month, BigDecimal due, BigDecimal paid, BigDecimal remaining) {
    }

    public record PayoffPoint(String date, BigDecimal remaining, BigDecimal interest) {
    }

    public record CashflowAlert(
            int upcomingCount,
            BigDecimal upcomingTotal,
            BigDecimal projectedBalance,
            String message,
            String level) {
    }

    public record ReminderResponse(
            Long id,
            Long debtId,
            String debtName,
            Long scheduleId,
            ReminderType remindType,
            String channel,
            LocalDate remindAt,
            Integer priority,
            ReminderStatus status,
            LocalDateTime sentAt,
            String message) {
    }
}
