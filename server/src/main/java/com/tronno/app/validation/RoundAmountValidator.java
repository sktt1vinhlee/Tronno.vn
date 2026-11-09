package com.tronno.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * RoundAmountValidator
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

public class RoundAmountValidator implements ConstraintValidator<RoundAmount, BigDecimal> {
    private static final BigDecimal STEP = new BigDecimal("1000");

    /**
     * Kiểm tra số tiền là số tròn và tối thiểu 1.000 đồng.
     *
     * @param value số tiền cần kiểm tra
     * @param context context kiểm tra
     * @return boolean true nếu hợp lệ
     */
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.compareTo(STEP) < 0) {
            return false;
        }
        // Chia hết cho 1000 => phần dư = 0
        return value.remainder(STEP).compareTo(BigDecimal.ZERO) == 0;
    }
}
