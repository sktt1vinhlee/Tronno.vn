package com.tronno.app.controllers;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tronno.app.dtos.TrickDtos.*;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.LetterService;

/**
 * LetterController
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
public class LetterController {
    private final LetterService letterService;

    /**
     * Soạn thư bút nợ.
     *
     * @param a đối tượng xác thực
     * @param r yêu cầu soạn thư
     * @return LetterResponse nội dung thư
     */
    @PostMapping("/letters/compose")
    public LetterResponse compose(Authentication a, @Valid @RequestBody LetterRequest r) {
        return letterService.composeLetter(CurrentUser.id(a), r);
    }

    /**
     * Kiểm tra lãi suất có vượt trần pháp luật hay không.
     *
     * @param a đối tượng xác thực
     * @param r thông tin lãi suất
     * @return RateCheckResponse kết quả kiểm tra
     */
    @PostMapping("/letters/rate-check")
    public RateCheckResponse rateCheck(Authentication a, @Valid @RequestBody RateCheckRequest r) {
        return letterService.checkRate(CurrentUser.id(a), r);
    }
}
