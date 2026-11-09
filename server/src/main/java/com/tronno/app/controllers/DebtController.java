package com.tronno.app.controllers;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.service.DebtService;

/**
 * DebtController
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

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {
    private final DebtService debtService;

    /**
     * Lấy danh sách khoản nợ theo trạng thái.
     *
     * @param a đối tượng xác thực
     * @param status trạng thái lọc, có thể null
     * @return List khoản nợ
     */
    @GetMapping
    public List<DebtResponse> list(Authentication a,
                                    @RequestParam(required = false) DebtStatus status) {
        return debtService.listDebts(userId(a), status);
    }

    /**
     * Lấy chi tiết một khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @return DebtResponse thông tin khoản nợ
     */
    @GetMapping("/{id}")
    public DebtResponse get(Authentication a, @PathVariable Long id) {
        return debtService.getDebt(userId(a), id);
    }

    /**
     * Tạo mới khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param r thông tin khoản nợ
     * @return DebtResponse khoản nợ vừa tạo
     */
    @PostMapping
    public DebtResponse create(Authentication a, @Valid @RequestBody DebtRequest r) {
        return debtService.createDebt(userId(a), r);
    }

    /**
     * Cập nhật khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @param r thông tin mới
     * @return DebtResponse khoản nợ đã cập nhật
     */
    @PutMapping("/{id}")
    public DebtResponse update(Authentication a, @PathVariable Long id,
                               @Valid @RequestBody DebtRequest r) {
        return debtService.updateDebt(userId(a), id, r);
    }

    /**
     * Đổi trạng thái khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @param status trạng thái mới
     * @return DebtResponse khoản nợ đã cập nhật
     */
    @PatchMapping("/{id}/status")
    public DebtResponse changeStatus(Authentication a, @PathVariable Long id,
                                     @RequestParam DebtStatus status) {
        debtService.closeDebt(userId(a), id, status);
        return debtService.getDebt(userId(a), id);
    }

    /**
     * Xoá khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     */
    @DeleteMapping("/{id}")
    public void delete(Authentication a, @PathVariable Long id) {
        debtService.deleteDebt(userId(a), id);
    }

    /**
     * Lấy lịch trả của khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @return List kỳ trả
     */
    @GetMapping("/{id}/schedule")
    public List<ScheduleResponse> schedule(Authentication a, @PathVariable Long id) {
        return debtService.listSchedule(userId(a), id);
    }

    /**
     * Lấy lịch sử thanh toán của khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @return List khoản trả
     */
    @GetMapping("/{id}/payments")
    public List<PaymentResponse> payments(Authentication a, @PathVariable Long id) {
        return debtService.listPayments(userId(a), id);
    }

    /**
     * Ghi nhận thanh toán cho khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @param r thông tin thanh toán
     * @return PaymentResponse khoản trả vừa ghi nhận
     */
    @PostMapping("/{id}/payments")
    public PaymentResponse pay(Authentication a, @PathVariable Long id,
                               @Valid @RequestBody PaymentRequest r) {
        return debtService.recordPayment(userId(a), id, r);
    }

    /**
     * Đảo bút toán khoản trả.
     *
     * @param a đối tượng xác thực
     * @param paymentId id khoản trả
     * @param r lý do đảo, có thể null
     * @return PaymentResponse khoản trả sau khi đảo
     */
    @PostMapping("/payments/{paymentId}/reverse")
    public PaymentResponse reverse(Authentication a, @PathVariable Long paymentId,
                                   @RequestBody(required = false) ReversalRequest r) {
        String reason = r == null ? null : r.reason();
        return debtService.reversePayment(userId(a), paymentId, reason);
    }

    /**
     * Lấy lịch sử thay đổi lãi suất.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @return List lịch sử lãi suất
     */
    @GetMapping("/{id}/rate-history")
    public List<RateChangeResponse> rateHistory(Authentication a, @PathVariable Long id) {
        return debtService.listRateHistory(userId(a), id);
    }

    /**
     * Thay đổi lãi suất của khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @param r thông tin lãi suất mới
     * @return DebtResponse khoản nợ đã cập nhật
     */
    @PostMapping("/{id}/rate-history")
    public DebtResponse changeRate(Authentication a, @PathVariable Long id,
                                   @Valid @RequestBody RateChangeRequest r) {
        return debtService.changeRate(userId(a), id, r);
    }

    /**
     * Gia hạn khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param id id khoản nợ
     * @param r thông tin gia hạn
     * @return DebtResponse khoản nợ sau gia hạn
     */
    @PostMapping("/{id}/extend")
    public DebtResponse extend(Authentication a, @PathVariable Long id,
                               @Valid @RequestBody ExtensionRequest r) {
        return debtService.extendDebt(userId(a), id, r);
    }

    /**
     * Lấy id người dùng từ đối tượng xác thực.
     *
     * @param a đối tượng xác thực
     * @return Long id người dùng
     */
    private Long userId(Authentication a) {
        return com.tronno.app.security.CurrentUser.id(a);
    }
}
