package com.tronno.app.controllers;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.ReportService;

/**
 * ReportController
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    /**
     * Lấy số liệu bảng điều khiển.
     *
     * @param a đối tượng xác thực
     * @return DashboardResponse số liệu tổng hợp
     */
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(Authentication a) {
        return reportService.dashboard(CurrentUser.id(a));
    }

    /**
     * Lấy thống kê dòng tiền theo tháng.
     *
     * @param a đối tượng xác thực
     * @param months số tháng, mặc định 12
     * @return List dòng tiền theo tháng
     */
    @GetMapping("/reports/cashflow")
    public List<CashflowMonth> cashflow(Authentication a,
                                        @RequestParam(defaultValue = "12") int months) {
        return reportService.monthlyCashflow(CurrentUser.id(a), Math.max(1, Math.min(months, 60)));
    }

    /**
     * Lấy toàn bộ lịch sử thanh toán.
     *
     * @param a đối tượng xác thực
     * @return List khoản trả
     */
    @GetMapping("/reports/payments")
    public List<PaymentResponse> payments(Authentication a) {
        return reportService.listAllPayments(CurrentUser.id(a));
    }
}
