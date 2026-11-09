package com.tronno.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Kiểm tra số tiền là số tròn (chia hết cho 1000) và không nhỏ hơn 1000.
 * <p>Ví dụ hợp lệ: 35000, 60000, 1200000. Ví dụ không hợp lệ: 35500, 500, 0.
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
@Documented
@Constraint(validatedBy = RoundAmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RoundAmount {
    String message() default "Số tiền phải là số tròn (chia hết cho 1000) và tối thiểu 1000";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
