package com.tronno.app.controllers;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.DebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/creditors")
@RequiredArgsConstructor
public class CreditorController {
    private final DebtService debtService;

    @GetMapping
    public List<CreditorResponse> list(Authentication a) {
        return debtService.listCreditors(CurrentUser.id(a));
    }

    @PostMapping
    public CreditorResponse create(Authentication a, @Valid @RequestBody CreditorRequest r) {
        return debtService.createCreditor(CurrentUser.id(a), r);
    }

    @PutMapping("/{id}")
    public CreditorResponse update(Authentication a, @PathVariable Long id,
                                   @Valid @RequestBody CreditorRequest r) {
        return debtService.updateCreditor(CurrentUser.id(a), id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication a, @PathVariable Long id) {
        debtService.deleteCreditor(CurrentUser.id(a), id);
    }
}
