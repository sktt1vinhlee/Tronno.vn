package com.tronno.app.controllers;

import com.tronno.app.dtos.DebtDtos.ReminderResponse;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;

    @GetMapping("/reminders")
    public List<ReminderResponse> list(Authentication a) {
        return reminderService.list(CurrentUser.id(a));
    }

    @GetMapping("/debts/{debtId}/reminders")
    public List<ReminderResponse> listByDebt(Authentication a, @PathVariable Long debtId) {
        return reminderService.listByDebt(CurrentUser.id(a), debtId);
    }

    @PostMapping("/reminders/{id}/send")
    public ReminderResponse sendNow(Authentication a, @PathVariable Long id) {
        return reminderService.sendNow(CurrentUser.id(a), id);
    }
}
