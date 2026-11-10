package com.tronno.app.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tronno.app.dtos.DebtDtos.DebtResponse;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.models.DebtType;

/**
 * DebtControllerResponseFactory
 *
 * <p>Tạo dữ liệu mẫu cho các test của {@link DebtController}.</p>
 *
 * Version 1.0
 *
 * Date: 10-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 10-11-2026         VinhHL             Create
 */
public final class DebtControllerResponseFactory {

    private DebtControllerResponseFactory() {
    }

    /**
     * Tạo response khoản nợ mẫu.
     *
     * @return response khoản nợ mẫu
     */
    public static DebtResponse response() {
        return new DebtResponse(ControllerTestData.DEBT_ID, "Vay mua xe", DebtType.BANK_LOAN, 20L,
                "Nguyễn Văn A", new BigDecimal("12000000"), BigDecimal.ZERO, null, null,
                LocalDate.of(2026, 1, 1), 12, null, null, new BigDecimal("1000000"),
                null, null, null, null, null, null, 0, DebtStatus.ACTIVE, null, null,
                new BigDecimal("12000000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDate.of(2026, 2, 1), new BigDecimal("1000000"), LocalDate.of(2027, 1, 1));
    }
}
