package com.tronno.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class RoundAmountValidator implements ConstraintValidator<RoundAmount, BigDecimal> {
    private static final BigDecimal STEP = new BigDecimal("1000");

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
