package com.tronno.app.controllers;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.service.DebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {
    private final DebtService debtService;

    @GetMapping
    public List<DebtResponse> list(Authentication a,
                                    @RequestParam(required = false) DebtStatus status) {
        return debtService.listDebts(userId(a), status);
    }

    @GetMapping("/{id}")
    public DebtResponse get(Authentication a, @PathVariable Long id) {
        return debtService.getDebt(userId(a), id);
    }

    @PostMapping
    public DebtResponse create(Authentication a, @Valid @RequestBody DebtRequest r) {
        return debtService.createDebt(userId(a), r);
    }

    @PutMapping("/{id}")
    public DebtResponse update(Authentication a, @PathVariable Long id,
                               @Valid @RequestBody DebtRequest r) {
        return debtService.updateDebt(userId(a), id, r);
    }

    @PatchMapping("/{id}/status")
    public DebtResponse changeStatus(Authentication a, @PathVariable Long id,
                                     @RequestParam DebtStatus status) {
        debtService.closeDebt(userId(a), id, status);
        return debtService.getDebt(userId(a), id);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication a, @PathVariable Long id) {
        debtService.deleteDebt(userId(a), id);
    }

    @GetMapping("/{id}/schedule")
    public List<ScheduleResponse> schedule(Authentication a, @PathVariable Long id) {
        return debtService.listSchedule(userId(a), id);
    }

    @GetMapping("/{id}/payments")
    public List<PaymentResponse> payments(Authentication a, @PathVariable Long id) {
        return debtService.listPayments(userId(a), id);
    }

    @PostMapping("/{id}/payments")
    public PaymentResponse pay(Authentication a, @PathVariable Long id,
                               @Valid @RequestBody PaymentRequest r) {
        return debtService.recordPayment(userId(a), id, r);
    }

    @PostMapping("/payments/{paymentId}/reverse")
    public PaymentResponse reverse(Authentication a, @PathVariable Long paymentId,
                                   @RequestBody(required = false) ReversalRequest r) {
        String reason = r == null ? null : r.reason();
        return debtService.reversePayment(userId(a), paymentId, reason);
    }

    @GetMapping("/{id}/rate-history")
    public List<RateChangeResponse> rateHistory(Authentication a, @PathVariable Long id) {
        return debtService.listRateHistory(userId(a), id);
    }

    @PostMapping("/{id}/rate-history")
    public DebtResponse changeRate(Authentication a, @PathVariable Long id,
                                   @Valid @RequestBody RateChangeRequest r) {
        return debtService.changeRate(userId(a), id, r);
    }

    @PostMapping("/{id}/extend")
    public DebtResponse extend(Authentication a, @PathVariable Long id,
                               @Valid @RequestBody ExtensionRequest r) {
        return debtService.extendDebt(userId(a), id, r);
    }

    private Long userId(Authentication a) {
        return com.tronno.app.security.CurrentUser.id(a);
    }
}
