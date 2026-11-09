package com.tronno.app.controllers;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tronno.app.dtos.DebtDtos.ReminderResponse;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.ReminderService;

/**
 * ReminderController
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
public class ReminderController {
    private final ReminderService reminderService;

    /**
     * Lấy toàn bộ nhắc hạn của người dùng.
     *
     * @param a đối tượng xác thực
     * @return List nhắc hạn
     */
    @GetMapping("/reminders")
    public List<ReminderResponse> list(Authentication a) {
        return reminderService.list(CurrentUser.id(a));
    }

    /**
     * Lấy nhắc hạn của một khoản nợ.
     *
     * @param a đối tượng xác thực
     * @param debtId id khoản nợ
     * @return List nhắc hạn
     */
    @GetMapping("/debts/{debtId}/reminders")
    public List<ReminderResponse> listByDebt(Authentication a, @PathVariable Long debtId) {
        return reminderService.listByDebt(CurrentUser.id(a), debtId);
    }

    /**
     * Gửi ngay một nhắc hạn.
     *
     * @param a đối tượng xác thực
     * @param id id nhắc hạn
     * @return ReminderResponse nhắc hạn sau khi gửi
     */
    @PostMapping("/reminders/{id}/send")
    public ReminderResponse sendNow(Authentication a, @PathVariable Long id) {
        return reminderService.sendNow(CurrentUser.id(a), id);
    }
}
