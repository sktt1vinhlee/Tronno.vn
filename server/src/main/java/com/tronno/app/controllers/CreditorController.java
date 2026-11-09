package com.tronno.app.controllers;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tronno.app.dtos.DebtDtos.*;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.DebtService;

/**
 * CreditorController
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
@RequestMapping("/api/creditors")
@RequiredArgsConstructor
public class CreditorController {
    private final DebtService debtService;

    /**
     * Lấy danh sách chủ nợ.
     *
     * @param a đối tượng xác thực
     * @return List chủ nợ
     */
    @GetMapping
    public List<CreditorResponse> list(Authentication a) {
        return debtService.listCreditors(CurrentUser.id(a));
    }

    /**
     * Tạo mới chủ nợ.
     *
     * @param a đối tượng xác thực
     * @param r thông tin chủ nợ
     * @return CreditorResponse chủ nợ vừa tạo
     */
    @PostMapping
    public CreditorResponse create(Authentication a, @Valid @RequestBody CreditorRequest r) {
        return debtService.createCreditor(CurrentUser.id(a), r);
    }

    /**
     * Cập nhật chủ nợ.
     *
     * @param a đối tượng xác thực
     * @param id id chủ nợ
     * @param r thông tin mới
     * @return CreditorResponse chủ nợ đã cập nhật
     */
    @PutMapping("/{id}")
    public CreditorResponse update(Authentication a, @PathVariable Long id,
                                   @Valid @RequestBody CreditorRequest r) {
        return debtService.updateCreditor(CurrentUser.id(a), id, r);
    }

    /**
     * Xoá chủ nợ.
     *
     * @param a đối tượng xác thực
     * @param id id chủ nợ
     */
    @DeleteMapping("/{id}")
    public void delete(Authentication a, @PathVariable Long id) {
        debtService.deleteCreditor(CurrentUser.id(a), id);
    }
}
