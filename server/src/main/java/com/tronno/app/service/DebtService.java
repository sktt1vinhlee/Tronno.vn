package com.tronno.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.exception.ResourceNotFoundException;
import com.tronno.app.models.*;
import com.tronno.app.repository.*;
import com.tronno.app.service.finance.DateRules;
import com.tronno.app.service.finance.Money;
import com.tronno.app.service.finance.PaymentAllocator;
import com.tronno.app.service.finance.ScheduleGenerator;

/**
 * DebtService
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
@Service
@RequiredArgsConstructor
public class DebtService {
    private final DebtRepository debts;
    private final CreditorRepository creditors;
    private final RepaymentScheduleRepository schedules;
    private final PaymentRepository payments;
    private final PaymentAllocationRepository allocations;
    private final InterestRateHistoryRepository rateHistory;
    private final ReminderRepository reminders;

    /**
     * Lấy danh sách chủ nợ của người dùng.
     *
     * @param userId id người dùng
     * @return List chủ nợ
     */
    @Transactional(readOnly = true)
    public List<CreditorResponse> listCreditors(Long userId) {
        return creditors.findByUserIdOrderByNameAsc(userId).stream()
                .map(CreditorResponse::from).toList();
    }

    /**
     * Tạo mới một chủ nợ.
     *
     * @param userId id người dùng
     * @param r thông tin chủ nợ
     * @return CreditorResponse chủ nợ vừa tạo
     */
    @Transactional
    public CreditorResponse createCreditor(Long userId, CreditorRequest r) {
        Creditor c = new Creditor(userId, r.name().trim(),
                r.kind() == null ? CreditorKind.PERSON : r.kind());
        c.setPhone(blankToNull(r.phone()));
        c.setEmail(blankToNull(r.email()));
        c.setNote(blankToNull(r.note()));
        creditors.save(c);
        return CreditorResponse.from(c);
    }

    /**
     * Cập nhật thông tin chủ nợ.
     *
     * @param userId id người dùng
     * @param id id chủ nợ
     * @param r thông tin mới
     * @return CreditorResponse chủ nợ đã cập nhật
     */
    @Transactional
    public CreditorResponse updateCreditor(Long userId, Long id, CreditorRequest r) {
        Creditor c = creditors.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ nợ"));
        c.setName(r.name().trim());
        if (r.kind() != null) c.setKind(r.kind());
        c.setPhone(blankToNull(r.phone()));
        c.setEmail(blankToNull(r.email()));
        c.setNote(blankToNull(r.note()));
        creditors.save(c);
        return CreditorResponse.from(c);
    }

    /**
     * Xoá một chủ nợ của người dùng.
     *
     * @param userId id người dùng
     * @param id id chủ nợ
     */
    @Transactional
    public void deleteCreditor(Long userId, Long id) {
        Creditor c = creditors.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ nợ"));
        creditors.delete(c);
    }

    /**
     * Tạo mới một khoản nợ cùng lịch trả.
     *
     * @param userId id người dùng
     * @param r thông tin khoản nợ
     * @return DebtResponse khoản nợ vừa tạo
     */
    @Transactional
    public DebtResponse createDebt(Long userId, DebtRequest r) {
        Long creditorId = resolveCreditorId(userId, r);
        Debt d = buildDebt(userId, r);
        d.setCreditorId(creditorId);
        applyTerm(d, r);
        debts.save(d);

        List<RepaymentSchedule> schedule = ScheduleGenerator.generate(
                d, List.of(), Money.zero(), Money.zero());
        schedule.forEach(s -> s.setDebtId(d.getId()));
        schedules.saveAll(schedule);

        rateHistory.save(InterestRateHistory.builder()
                .debtId(d.getId())
                .annualRate(d.getAnnualRate())
                .effectiveFrom(d.getDisbursementDate())
                .note("Lãi suất ban đầu")
                .build());

        return toResponse(d);
    }

    /**
     * Xác định chủ nợ của khoản nợ:
     * <ul>
     *   <li>Nếu đã chọn chủ nợ có sẵn: dùng id đó (và kiểm tra thuộc về user).</li>
     *   <li>Nếu nhập tay tên chủ nợ: tự tạo chủ nợ mới.</li>
     *   <li>Nếu cả hai đều rỗng: khoản nợ không gắn với chủ nợ nào.</li>
     * </ul>
     *
     * @param userId id người dùng
     * @param r thông tin khoản nợ
     * @return Long id chủ nợ, có thể null
     */
    private Long resolveCreditorId(Long userId, DebtRequest r) {
        if (r.creditorId() != null) {
            Creditor c = creditors.findByIdAndUserId(r.creditorId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ nợ"));
            return c.getId();
        }
        if (r.creditorName() != null && !r.creditorName().isBlank()) {
            Creditor c = new Creditor(userId, r.creditorName().trim(),
                    r.creditorKind() == null ? CreditorKind.PERSON : r.creditorKind());
            c.setPhone(blankToNull(r.creditorPhone()));
            c.setEmail(blankToNull(r.creditorEmail()));
            creditors.save(c);
            return c.getId();
        }
        return null;
    }

    /**
     * Lấy danh sách khoản nợ theo trạng thái.
     *
     * @param userId id người dùng
     * @param status trạng thái lọc, có thể null
     * @return List khoản nợ
     */
    @Transactional(readOnly = true)
    public List<DebtResponse> listDebts(Long userId, DebtStatus status) {
        List<Debt> list = status == null
                ? debts.findByUserIdOrderByCreatedAtDesc(userId)
                : debts.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        return list.stream().map(this::toResponse).toList();
    }

    /**
     * Lấy chi tiết một khoản nợ.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @return DebtResponse thông tin khoản nợ
     */
    @Transactional(readOnly = true)
    public DebtResponse getDebt(Long userId, Long debtId) {
        return toResponse(requireDebt(userId, debtId));
    }

    /**
     * Cập nhật khoản nợ và sinh lại lịch trả nếu cần.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @param r thông tin mới
     * @return DebtResponse khoản nợ đã cập nhật
     */
    @Transactional
    public DebtResponse updateDebt(Long userId, Long debtId, DebtRequest r) {
        Debt d = requireDebt(userId, debtId);
        d.setName(r.name().trim());
        d.setDebtType(r.debtType());
        Long creditorId = resolveCreditorId(userId, r);
        if (creditorId != null) d.setCreditorId(creditorId);
        d.setPrincipal(Money.of(r.principal()));
        d.setAnnualRate(r.annualRate());
        if (r.interestMethod() != null) d.setInterestMethod(r.interestMethod());
        if (r.scheduleType() != null) d.setScheduleType(r.scheduleType());
        d.setDisbursementDate(r.disbursementDate());
        d.setTermMonths(r.termMonths());
        applyTerm(d, r);
        if (r.lateFeeType() != null) d.setLateFeeType(r.lateFeeType());
        if (r.lateFeeRate() != null) d.setLateFeeRate(r.lateFeeRate());
        if (r.lateFeeAmount() != null) d.setLateFeeAmount(r.lateFeeAmount());
        if (r.lateFeeAmount() != null) d.setLateFeeAmount(r.lateFeeAmount());
        if (r.earlyFeeType() != null) d.setEarlyFeeType(r.earlyFeeType());
        if (r.earlyFeeRate() != null) d.setEarlyFeeRate(r.earlyFeeRate());
        if (r.earlyFeeAmount() != null) d.setEarlyFeeAmount(r.earlyFeeAmount());
        if (r.graceDays() != null) d.setGraceDays(r.graceDays());
        d.setNote(blankToNull(r.note()));
        d.setContractUrl(blankToNull(r.contractUrl()));
        d.setUpdatedAt(LocalDateTime.now());
        debts.save(d);

        recalculateSchedule(userId, d);
        return toResponse(d);
    }

    /**
     * Chuyển khoản nợ sang trạng thái đã đóng hoặc đã tất toán.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @param status trạng thái mới
     */
    @Transactional
    public void closeDebt(Long userId, Long debtId, DebtStatus status) {
        Debt d = requireDebt(userId, debtId);
        d.setStatus(status);
        d.setUpdatedAt(LocalDateTime.now());
        debts.save(d);
    }

    /**
     * Xoá một khoản nợ cùng lịch trả và lịch sử liên quan.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     */
    @Transactional
    public void deleteDebt(Long userId, Long debtId) {
        Debt d = requireDebt(userId, debtId);

        if (d.getStatus() == DebtStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Khoản nợ đang còn hiệu lực. Hãy đánh dấu 'Đã tất toán' hoặc 'Đóng hồ sơ' trước khi xoá.");
        }
        reminders.deleteByDebtId(debtId);
        schedules.deleteByDebtId(debtId);
        debts.delete(d);
    }

    /**
     * Lấy lịch trả của một khoản nợ.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @return List kỳ trả
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> listSchedule(Long userId, Long debtId) {
        Debt d = requireDebt(userId, debtId);
        LocalDate today = LocalDate.now();
        return schedules.findByDebtIdOrderByInstallmentNoAsc(debtId).stream()
                .map(s -> toScheduleResponse(s, d, today))
                .toList();
    }

    /**
     * Sinh lại toàn bộ lịch trả khi khoản nợ thay đổi, giữ nguyên phần đã trả.
     *
     * @param userId id người dùng
     * @param d khoản nợ
     * @return DebtResponse khoản nợ với lịch trả mới
     */
    @Transactional
    public DebtResponse recalculateSchedule(Long userId, Debt d) {
        List<RepaymentSchedule> existing = schedules.findByDebtIdOrderByInstallmentNoAsc(d.getId());
        BigDecimal paidPrincipal = existing.stream()
                .map(RepaymentSchedule::getPrincipalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidTotal = existing.stream()
                .map(RepaymentSchedule::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InterestRateHistory> history = rateHistory.findByDebtIdOrderByEffectiveFromAsc(d.getId());

        List<RepaymentSchedule> fresh = ScheduleGenerator.generate(
                d, history, paidPrincipal, paidTotal);

        Map<Integer, RepaymentSchedule> oldByNo = existing.stream()
                .collect(Collectors.toMap(RepaymentSchedule::getInstallmentNo, s -> s));

        for (RepaymentSchedule s : fresh) {
            RepaymentSchedule old = oldByNo.get(s.getInstallmentNo());
            if (old != null && old.getStatus() == ScheduleStatus.PAID) {
                s.setPrincipalPaid(old.getPrincipalPaid());
                s.setInterestPaid(old.getInterestPaid());
                s.setFeePaid(old.getFeePaid());
                s.setPaidAmount(old.getPaidAmount());
                s.setStatus(ScheduleStatus.PAID);
                s.setPaidAt(old.getPaidAt());
                s.setTotalDue(Money.add(Money.add(s.getPrincipalDue(), s.getInterestDue()), s.getFeeDue()));
            }
            s.setId(null);
            s.setDebtId(d.getId());
        }

        List<RepaymentSchedule> toDelete = existing.stream()
                .filter(o -> o.getStatus() != ScheduleStatus.PAID)
                .toList();
        schedules.deleteAll(toDelete);
        schedules.flush();

        schedules.saveAll(fresh);
        return toResponse(d);
    }

    /**
     * Ghi nhận một khoản trả và phân bổ vào các kỳ trả.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @param r thông tin khoản trả
     * @return PaymentResponse khoản trả vừa ghi nhận
     */
    @Transactional
    public PaymentResponse recordPayment(Long userId, Long debtId, PaymentRequest r) {
        Debt d = requireDebt(userId, debtId);
        LocalDate paidAt = r.paidAt() == null ? LocalDate.now() : r.paidAt();

        List<RepaymentSchedule> open = schedules
                .findByDebtIdAndStatusInOrderByInstallmentNoAsc(debtId,
                        List.of(ScheduleStatus.PENDING, ScheduleStatus.PARTIAL, ScheduleStatus.OVERDUE));

        PaymentAllocator.Result result = PaymentAllocator.allocate(
                r.amount(), open, d, paidAt, r.scheduleId());

        Payment p = Payment.builder()
                .debtId(debtId)
                .userId(userId)
                .amount(Money.of(r.amount()))
                .surplus(result.surplus())
                .paidAt(paidAt)
                .method(r.method() == null || r.method().isBlank() ? "CASH" : r.method())
                .note(blankToNull(r.note()))
                .status("RECORDED")
                .build();
        payments.save(p);

        Map<Long, List<PaymentAllocator.Allocation>> bySchedule = result.allocations().stream()
                .collect(Collectors.groupingBy(PaymentAllocator.Allocation::scheduleId));

        for (var entry : bySchedule.entrySet()) {
            RepaymentSchedule s = schedules.findById(entry.getKey()).orElse(null);
            if (s == null) continue;
            for (PaymentAllocator.Allocation a : entry.getValue()) {
                allocations.save(PaymentAllocation.builder()
                        .paymentId(p.getId())
                        .scheduleId(s.getId())
                        .component(a.component())
                        .amount(a.amount())
                        .build());
                switch (a.component()) {
                    case PaymentAllocator.COMPONENT_LATE_FEE, PaymentAllocator.COMPONENT_FEE ->
                            s.setFeePaid(Money.add(s.getFeePaid(), a.amount()));
                    case PaymentAllocator.COMPONENT_INTEREST ->
                            s.setInterestPaid(Money.add(s.getInterestPaid(), a.amount()));
                    case PaymentAllocator.COMPONENT_PRINCIPAL ->
                            s.setPrincipalPaid(Money.add(s.getPrincipalPaid(), a.amount()));
                    default -> {
                    }
                }
            }
            s.setPaidAmount(Money.add(
                    Money.add(s.getPrincipalPaid(), s.getInterestPaid()), s.getFeePaid()));
            s.setStatus(resolveStatus(s, paidAt));
            if (s.getStatus() == ScheduleStatus.PAID) s.setPaidAt(paidAt);
            schedules.save(s);
        }

        refreshDebtStatus(d);

        List<AllocationResponse> allocationResponses = result.allocations().stream()
                .map(a -> {
                    RepaymentSchedule s = schedules.findById(a.scheduleId()).orElse(null);
                    return new AllocationResponse(a.scheduleId(),
                            s == null ? null : s.getInstallmentNo(),
                            s == null ? null : s.getDueDate(),
                            a.component(), a.amount());
                })
                .toList();

        return new PaymentResponse(p.getId(), debtId, d.getName(), p.getAmount(),
                result.applied(), result.surplus(), paidAt, p.getMethod(), p.getNote(),
                p.getStatus(), allocationResponses);
    }

    /**
     * Đảo bút toán một khoản trả đã ghi nhận.
     *
     * @param userId id người dùng
     * @param paymentId id khoản trả
     * @param reason lý do đảo bút toán
     * @return PaymentResponse khoản trả sau khi đảo
     */
    @Transactional
    public PaymentResponse reversePayment(Long userId, Long paymentId, String reason) {
        Payment original = payments.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản trả"));
        Debt d = requireDebt(userId, original.getDebtId());

        if ("REVERSED".equals(original.getStatus())) {
            throw new IllegalArgumentException("Khoản trả này đã được đảo trước đó.");
        }

        List<PaymentAllocation> allocs = allocations.findByPaymentIdOrderByIdAsc(paymentId);
        Map<Long, RepaymentSchedule> touched = new LinkedHashMap<>();
        for (PaymentAllocation a : allocs) {
            RepaymentSchedule s = schedules.findById(a.getScheduleId()).orElse(null);
            if (s == null) continue;
            switch (a.getComponent()) {
                case PaymentAllocator.COMPONENT_LATE_FEE, PaymentAllocator.COMPONENT_FEE ->
                        s.setFeePaid(Money.subtract(s.getFeePaid(), a.getAmount()));
                case PaymentAllocator.COMPONENT_INTEREST ->
                        s.setInterestPaid(Money.subtract(s.getInterestPaid(), a.getAmount()));
                case PaymentAllocator.COMPONENT_PRINCIPAL ->
                        s.setPrincipalPaid(Money.subtract(s.getPrincipalPaid(), a.getAmount()));
                default -> {
                }
            }
            s.setPaidAmount(Money.add(
                    Money.add(s.getPrincipalPaid(), s.getInterestPaid()), s.getFeePaid()));
            s.setStatus(resolveStatus(s, LocalDate.now()));
            if (s.getStatus() != ScheduleStatus.PAID) s.setPaidAt(null);
            schedules.save(s);
            touched.put(s.getId(), s);
        }

        Payment reversal = Payment.builder()
                .debtId(original.getDebtId())
                .userId(userId)
                .amount(Money.of(original.getAmount()).negate())
                .surplus(Money.zero())
                .paidAt(original.getPaidAt())
                .method(original.getMethod())
                .note("Bút toán đảo #" + original.getId()
                        + (reason == null || reason.isBlank() ? "" : " - " + reason))
                .status("RECORDED")
                .reversalOf(original.getId())
                .build();
        payments.save(reversal);

        original.setStatus("REVERSED");
        payments.save(original);

        refreshDebtStatus(d);

        return new PaymentResponse(reversal.getId(), original.getDebtId(), d.getName(),
                reversal.getAmount(), Money.zero(), Money.zero(), reversal.getPaidAt(),
                reversal.getMethod(), reversal.getNote(), reversal.getStatus(), List.of());
    }

    /**
     * Lấy lịch sử thanh toán của một khoản nợ.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @return List khoản trả
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments(Long userId, Long debtId) {
        Debt d = requireDebt(userId, debtId);
        Map<Long, RepaymentSchedule> scheduleMap = schedules.findByDebtIdOrderByInstallmentNoAsc(debtId)
                .stream().collect(Collectors.toMap(RepaymentSchedule::getId, s -> s));

        return payments.findByDebtIdOrderByPaidAtDescIdDesc(debtId).stream()
                .map(p -> {
                    List<AllocationResponse> list = allocations.findByPaymentIdOrderByIdAsc(p.getId())
                            .stream().map(a -> {
                                RepaymentSchedule s = scheduleMap.get(a.getScheduleId());
                                return new AllocationResponse(a.getScheduleId(),
                                        s == null ? null : s.getInstallmentNo(),
                                        s == null ? null : s.getDueDate(),
                                        a.getComponent(), a.getAmount());
                            }).toList();
                    return new PaymentResponse(p.getId(), debtId, d.getName(), p.getAmount(),
                            Money.zero(), p.getSurplus(), p.getPaidAt(), p.getMethod(),
                            p.getNote(), p.getStatus(), list);
                }).toList();
    }

    /**
     * Thay đổi lãi suất của khoản nợ và sinh lại lịch trả.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @param r thông tin lãi suất mới
     * @return DebtResponse khoản nợ đã cập nhật
     */
    @Transactional
    public DebtResponse changeRate(Long userId, Long debtId, RateChangeRequest r) {
        Debt d = requireDebt(userId, debtId);
        LocalDate from = r.effectiveFrom();

        List<InterestRateHistory> history = rateHistory.findByDebtIdOrderByEffectiveFromAsc(debtId);
        for (InterestRateHistory h : history) {
            if (h.getEffectiveTo() == null && h.getEffectiveFrom().isBefore(from)) {
                h.setEffectiveTo(from.minusDays(1));
                rateHistory.save(h);
            }
        }

        rateHistory.save(InterestRateHistory.builder()
                .debtId(debtId)
                .annualRate(r.annualRate())
                .effectiveFrom(from)
                .note(blankToNull(r.note()))
                .build());

        d.setAnnualRate(r.annualRate());
        d.setUpdatedAt(LocalDateTime.now());
        debts.save(d);

        recalculateSchedule(userId, d);
        return toResponse(d);
    }

    /**
     * Lấy lịch sử thay đổi lãi suất của khoản nợ.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @return List lịch sử lãi suất
     */
    @Transactional(readOnly = true)
    public List<RateChangeResponse> listRateHistory(Long userId, Long debtId) {
        requireDebt(userId, debtId);
        return rateHistory.findByDebtIdOrderByEffectiveFromAsc(debtId).stream()
                .map(h -> new RateChangeResponse(h.getId(), h.getAnnualRate(),
                        h.getEffectiveFrom(), h.getEffectiveTo(), h.getNote()))
                .toList();
    }

    /**
     * Gia hạn khoản nợ thêm số tháng yêu cầu.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @param r thông tin gia hạn
     * @return DebtResponse khoản nợ sau gia hạn
     */
    @Transactional
    public DebtResponse extendDebt(Long userId, Long debtId, ExtensionRequest r) {
        Debt d = requireDebt(userId, debtId);
        d.setTermMonths(d.getTermMonths() + r.extraMonths());
        if (r.newAnnualRate() != null) {
            d.setAnnualRate(r.newAnnualRate());
            rateHistory.save(InterestRateHistory.builder()
                    .debtId(debtId)
                    .annualRate(r.newAnnualRate())
                    .effectiveFrom(LocalDate.now())
                    .note("Gia hạn khoản nợ")
                    .build());
        }
        d.setUpdatedAt(LocalDateTime.now());
        debts.save(d);
        recalculateSchedule(userId, d);
        return toResponse(d);
    }

    /**
     * Lấy khoản nợ của người dùng hoặc báo lỗi nếu không tồn tại.
     *
     * @param userId id người dùng
     * @param debtId id khoản nợ
     * @return Debt khoản nợ tìm thấy
     */
    private Debt requireDebt(Long userId, Long debtId) {
        return debts.findByIdAndUserId(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản nợ"));
    }

    /**
     * Tạo entity khoản nợ từ DTO.
     *
     * @param userId id người dùng
     * @param r thông tin khoản nợ
     * @return Debt entity khoản nợ
     */
    private Debt buildDebt(Long userId, DebtRequest r) {
        Debt d = new Debt();
        d.setUserId(userId);
        d.setName(r.name().trim());
        d.setDebtType(r.debtType());
        d.setPrincipal(Money.of(r.principal()));
        d.setAnnualRate(r.annualRate());
        d.setInterestMethod(r.interestMethod() == null ? InterestMethod.REDUCING : r.interestMethod());
        d.setScheduleType(r.scheduleType() == null ? ScheduleType.EQUAL_PRINCIPAL : r.scheduleType());
        d.setDisbursementDate(r.disbursementDate());
        d.setTermMonths(r.termMonths());
        d.setLateFeeType(r.lateFeeType() == null ? LateFeeType.NONE : r.lateFeeType());
        d.setLateFeeRate(r.lateFeeRate() == null ? BigDecimal.ZERO : r.lateFeeRate());
        d.setLateFeeAmount(r.lateFeeAmount() == null ? BigDecimal.ZERO : r.lateFeeAmount());
        d.setLateFeeAmount(r.lateFeeAmount() == null ? BigDecimal.ZERO : r.lateFeeAmount());
        d.setEarlyFeeType(r.earlyFeeType() == null ? EarlyFeeType.NONE : r.earlyFeeType());
        d.setEarlyFeeRate(r.earlyFeeRate() == null ? BigDecimal.ZERO : r.earlyFeeRate());
        d.setEarlyFeeAmount(r.earlyFeeAmount() == null ? BigDecimal.ZERO : r.earlyFeeAmount());
        d.setGraceDays(r.graceDays() == null ? 0 : r.graceDays());
        d.setStatus(DebtStatus.ACTIVE);
        d.setNote(blankToNull(r.note()));
        d.setContractUrl(blankToNull(r.contractUrl()));
        return d;
    }

    /**
     * Áp dụng kiểu thời hạn và ngày đáo hạn cho khoản nợ.
     * <ul>
     *   <li>{@link TermType#FIXED_DATE}: người dùng chọn ngày đáo hạn cụ thể.
     *       Hệ thống tự tính số tháng kỳ hạn = chênh lệch từ ngày giải ngân đến ngày đáo hạn.</li>
     *   <li>{@link TermType#QUARTERLY}: trả theo quý, mỗi kỳ 3 tháng một lần.</li>
     * </ul>
     *
     * @param d khoản nợ
     * @param r thông tin khoản nợ
     */
    private void applyTerm(Debt d, DebtRequest r) {
        TermType type = r.termType() == null ? TermType.FIXED_DATE : r.termType();
        d.setTermType(type);

        if (type == TermType.QUARTERLY) {
            d.setMaturityDate(null);
            // Kỳ hạn phải bội số của 3 tháng để khớp với các kỳ trả theo quý.
            int months = Math.max(3, r.termMonths() == null ? 12 : r.termMonths());
            int quarters = Math.max(1, Math.round(months / 3f));
            d.setTermMonths(quarters * 3);
            return;
        }

        LocalDate maturity = r.maturityDate();
        if (maturity == null) {
            d.setMaturityDate(null);
            return;
        }
        if (maturity.isBefore(d.getDisbursementDate())) {
            throw new IllegalArgumentException("Ngày đáo hạn không được trước ngày giải ngân.");
        }
        d.setMaturityDate(maturity);
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
                d.getDisbursementDate().withDayOfMonth(1),
                maturity.withDayOfMonth(1));
        if (months < 1) months = 1;
        d.setTermMonths((int) months);
    }

    /**
     * Xác định trạng thái của một kỳ trả tại ngày hiện tại.
     *
     * @param s kỳ trả
     * @param today ngày hiện tại
     * @return ScheduleStatus trạng thái kỳ trả
     */
    private ScheduleStatus resolveStatus(RepaymentSchedule s, LocalDate today) {
        BigDecimal paid = Money.add(Money.add(s.getPrincipalPaid(), s.getInterestPaid()), s.getFeePaid());
        BigDecimal total = Money.add(Money.add(s.getPrincipalDue(), s.getInterestDue()), s.getFeeDue());
        if (Money.compareTo(paid, total) >= 0) return ScheduleStatus.PAID;
        if (Money.isPositive(paid)) {
            return s.getDueDate().isBefore(today) ? ScheduleStatus.OVERDUE : ScheduleStatus.PARTIAL;
        }
        return s.getDueDate().isBefore(today) ? ScheduleStatus.OVERDUE : ScheduleStatus.PENDING;
    }

    /**
     * Cập nhật lại trạng thái khoản nợ dựa trên các kỳ trả.
     *
     * @param d khoản nợ
     */
    private void refreshDebtStatus(Debt d) {
        List<RepaymentSchedule> all = schedules.findByDebtIdOrderByInstallmentNoAsc(d.getId());
        boolean allPaid = all.stream().allMatch(s -> s.getStatus() == ScheduleStatus.PAID);
        if (allPaid && allPaid && d.getStatus() == DebtStatus.ACTIVE) {
            d.setStatus(DebtStatus.PAID);
            debts.save(d);
        }
    }

    /**
     * Chuyển đổi entity Debt thành DTO trả về cho client.
     *
     * @param d entity khoản nợ
     * @return DebtResponse thông tin khoản nợ
     */
    public DebtResponse toResponse(Debt d) {
        LocalDate today = LocalDate.now();
        List<RepaymentSchedule> list = schedules.findByDebtIdOrderByInstallmentNoAsc(d.getId());

        BigDecimal remainingPrincipal = list.stream()
                .map(s -> Money.subtract(s.getPrincipalDue(), s.getPrincipalPaid()))
                .reduce(BigDecimal.ZERO, Money::add);
        BigDecimal totalPaid = list.stream()
                .map(RepaymentSchedule::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterestPaid = list.stream()
                .map(RepaymentSchedule::getInterestPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFeePaid = list.stream()
                .map(RepaymentSchedule::getFeePaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RepaymentSchedule next = list.stream()
                .filter(s -> s.getStatus() == ScheduleStatus.PENDING
                        || s.getStatus() == ScheduleStatus.PARTIAL
                        || s.getStatus() == ScheduleStatus.OVERDUE)
                .min(Comparator.comparing(RepaymentSchedule::getDueDate))
                .orElse(null);

        BigDecimal nextAmount = next == null ? Money.zero()
                : Money.add(Money.add(
                        Money.subtract(next.getPrincipalDue(), next.getPrincipalPaid()),
                        Money.subtract(next.getInterestDue(), next.getInterestPaid())),
                        Money.subtract(next.getFeeDue(), next.getFeePaid()));

        LocalDate debtFreeDate = list.stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .map(RepaymentSchedule::getDueDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        String creditorName = d.getCreditorId() == null ? null
                : creditors.findById(d.getCreditorId()).map(Creditor::getName).orElse(null);

        // Số kỳ trả thực tế (trả theo quý thì mỗi kỳ 3 tháng).
        int stepMonths = d.getTermType() == TermType.QUARTERLY ? 3 : 1;
        int installments = Math.max(1, d.getTermMonths() / stepMonths);

        BigDecimal installment = d.getScheduleType() == ScheduleType.ANNUITY
                ? ScheduleGenerator.annuityPayment(d.getPrincipal(), d.getAnnualRate(), installments)
                : Money.divide(d.getPrincipal(), BigDecimal.valueOf(installments));

        return new DebtResponse(
                d.getId(), d.getName(), d.getDebtType(), d.getCreditorId(), creditorName,
                d.getPrincipal(), d.getAnnualRate(), d.getInterestMethod(), d.getScheduleType(),
                d.getDisbursementDate(), d.getTermMonths(), d.getTermType(), d.getMaturityDate(), installment,
                d.getLateFeeType(), d.getLateFeeRate(), d.getLateFeeAmount(),
                d.getEarlyFeeType(), d.getEarlyFeeRate(),
                d.getEarlyFeeAmount(), d.getGraceDays(), d.getStatus(), d.getNote(), d.getContractUrl(),
                remainingPrincipal, totalPaid, totalInterestPaid, totalFeePaid,
                next == null ? null : next.getDueDate(), nextAmount, debtFreeDate);
    }

    /**
     * Chuyển đổi entity RepaymentSchedule thành DTO kèm số tiền còn lại.
     *
     * @param s entity kỳ trả
     * @param d khoản nợ
     * @param today ngày hiện tại
     * @return ScheduleResponse thông tin kỳ trả
     */
    public ScheduleResponse toScheduleResponse(RepaymentSchedule s, Debt d, LocalDate today) {
        BigDecimal remaining = Money.subtract(s.getTotalDue(), s.getPaidAmount());
        BigDecimal lateFee = PaymentAllocator.currentLateFee(d, s, today);
        return new ScheduleResponse(s.getId(), s.getDebtId(), s.getInstallmentNo(),
                s.getPeriodStart(), s.getPeriodEnd(), s.getDueDate(),
                s.getPrincipalDue(), s.getInterestDue(), s.getFeeDue(), s.getTotalDue(),
                s.getPrincipalPaid(), s.getInterestPaid(), s.getFeePaid(), s.getPaidAmount(),
                remaining, lateFee, s.getStatus(), s.getPaidAt());
    }

    /**
     * Chuyển chuỗi rỗng thành null để lưu vào database.
     *
     * @param value chuỗi cần chuẩn hoá
     * @return String đã chuẩn hoá
     */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
