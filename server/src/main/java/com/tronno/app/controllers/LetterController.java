package com.tronno.app.controllers;

import com.tronno.app.dtos.TrickDtos.*;
import com.tronno.app.security.CurrentUser;
import com.tronno.app.service.LetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    /** Soạn văn bản thông báo cho chủ nợ. */
    @PostMapping("/letters/compose")
    public LetterResponse compose(Authentication a, @Valid @RequestBody LetterRequest r) {
        return letterService.composeLetter(CurrentUser.id(a), r);
    }

    @PostMapping("/letters/rate-check")
    public RateCheckResponse rateCheck(Authentication a, @Valid @RequestBody RateCheckRequest r) {
        return letterService.checkRate(CurrentUser.id(a), r);
    }
}
