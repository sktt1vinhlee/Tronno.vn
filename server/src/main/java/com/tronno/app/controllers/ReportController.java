package com.tronno.app.controllers;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(Authentication a) {
        return reportService.dashboard(CurrentUser.id(a));
    }

    @GetMapping("/reports/cashflow")
    public List<CashflowMonth> cashflow(Authentication a,
                                        @RequestParam(defaultValue = "12") int months) {
        return reportService.monthlyCashflow(CurrentUser.id(a), Math.max(1, Math.min(months, 60)));
    }

    @GetMapping("/reports/payments")
    public List<PaymentResponse> payments(Authentication a) {
        return reportService.listAllPayments(CurrentUser.id(a));
    }
}
