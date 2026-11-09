package com.tronno.app.dtos;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * TrickDtos
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
public class TrickDtos {
    public record LetterRequest(
            Long debtId,
            String tone) {
    }

    public record LetterResponse(
            String title,
            String body,
            String delivery,
            String disclaimer) {
    }

    public record RateCheckRequest(
            @NotNull(message = "Vui lòng nhập lãi suất")
            BigDecimal annualRate,
            @NotNull(message = "Vui lòng nhập số tiền vay")
            BigDecimal amount,
            Integer termMonths,
            Boolean appLoan) {
    }

    public record RateCheckResponse(
            BigDecimal annualRate,
            double legalCapPercent,
            boolean exceedsCap,
            String severity,
            String message,
            BigDecimal monthlyInterest,
            BigDecimal totalInterest,
            BigDecimal maxLegalMonthlyInterest,
            String guidance,
            String disclaimer) {
    }
}
