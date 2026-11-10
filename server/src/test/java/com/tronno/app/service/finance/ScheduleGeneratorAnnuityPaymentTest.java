package com.tronno.app.service.finance;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ScheduleGeneratorAnnuityPaymentTest
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
@DisplayName("ScheduleGenerator.annuityPayment() - Tính tiền trả đều")
class ScheduleGeneratorAnnuityPaymentTest {

    @Test
    @DisplayName("Lãi suất 0 thì chia đều gốc")
    void zeroRateDividesEvenly() {
        assertThat(ScheduleGenerator.annuityPayment(
                new BigDecimal("1200000"), BigDecimal.ZERO, 12))
                .isEqualByComparingTo("100000.00");
    }

    @Test
    @DisplayName("Gốc bằng 0 thì trả 0")
    void zeroPrincipalReturnsZero() {
        assertThat(ScheduleGenerator.annuityPayment(
                BigDecimal.ZERO, new BigDecimal("10"), 12))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Tiền trả mỗi kỳ lớn hơn gốc chia đều khi có lãi")
    void paymentIsHigherThanEvenShare() {
        BigDecimal even = new BigDecimal("100000.00");
        BigDecimal payment = ScheduleGenerator.annuityPayment(
                new BigDecimal("1200000"), new BigDecimal("12"), 12);

        assertThat(payment).isGreaterThan(even);
    }
}
