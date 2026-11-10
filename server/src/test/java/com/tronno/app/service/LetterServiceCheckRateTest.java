package com.tronno.app.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.dtos.TrickDtos.RateCheckRequest;
import com.tronno.app.dtos.TrickDtos.RateCheckResponse;
import com.tronno.app.repository.DebtRepository;
import com.tronno.app.repository.RepaymentScheduleRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LetterServiceCheckRateTest
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
@ExtendWith(MockitoExtension.class)
@DisplayName("LetterService.checkRate() - Kiểm tra lãi suất hợp pháp")
class LetterServiceCheckRateTest {

    @Mock
    private DebtRepository debts;

    @Mock
    private RepaymentScheduleRepository schedules;

    @InjectMocks
    private LetterService service;

    @Test
    @DisplayName("Lãi suất dưới trần cho mức OK")
    void lowRateIsOk() {
        RateCheckRequest r = new RateCheckRequest(
                new BigDecimal("8"), new BigDecimal("10000000"), 12, false);

        RateCheckResponse res = service.checkRate(1L, r);

        assertThat(res.severity()).isEqualTo("OK");
        assertThat(res.exceedsCap()).isFalse();
        assertThat(res.legalCapPercent()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Lãi suất từ 15% trở lên cho mức WARNING")
    void mediumRateIsWarning() {
        RateCheckRequest r = new RateCheckRequest(
                new BigDecimal("15"), new BigDecimal("10000000"), 12, false);

        RateCheckResponse res = service.checkRate(1L, r);

        assertThat(res.severity()).isEqualTo("WARNING");
        assertThat(res.exceedsCap()).isFalse();
    }

    @Test
    @DisplayName("Lãi suất vượt 20% cho mức DANGER")
    void highRateIsDanger() {
        RateCheckRequest r = new RateCheckRequest(
                new BigDecimal("25"), new BigDecimal("10000000"), 12, false);

        RateCheckResponse res = service.checkRate(1L, r);

        assertThat(res.severity()).isEqualTo("DANGER");
        assertThat(res.exceedsCap()).isTrue();
        assertThat(res.message()).contains("298");
    }

    @Test
    @DisplayName("Tính tiền lãi theo công thức hiện tại của dịch vụ")
    void calculatesInterestAmounts() {
        RateCheckRequest r = new RateCheckRequest(
                new BigDecimal("12"), new BigDecimal("10000000"), 12, false);

        RateCheckResponse res = service.checkRate(1L, r);

        // Lưu ý: dịch vụ đang tính monthlyInterest = gốc x lãi suất x kỳ hạn / kỳ hạn,
        // tức bằng tiền lãi của 1 năm (10.000.000 x 12% = 1.200.000) chứ chưa phải lãi 1 tháng.
        assertThat(res.monthlyInterest()).isEqualByComparingTo("1200000.00");
        assertThat(res.totalInterest()).isEqualByComparingTo("14400000.00");
    }

    @Test
    @DisplayName("Trần pháp lý 20% cho mức lãi tối đa")
    void calculatesMaxLegalInterest() {
        RateCheckRequest r = new RateCheckRequest(
                new BigDecimal("12"), new BigDecimal("10000000"), 12, false);

        RateCheckResponse res = service.checkRate(1L, r);

        // 10.000.000 x 20% = 2.000.000
        assertThat(res.maxLegalMonthlyInterest()).isEqualByComparingTo("2000000.00");
    }

    @Test
    @DisplayName("Trần pháp lý là 20% mỗi năm")
    void legalCapIsTwentyPercent() {
        RateCheckRequest r = new RateCheckRequest(
                new BigDecimal("20"), new BigDecimal("10000000"), 12, false);

        RateCheckResponse res = service.checkRate(1L, r);

        assertThat(res.legalCapPercent()).isEqualTo(20.0);
        assertThat(res.exceedsCap()).isFalse();
    }

    @Test
    @DisplayName("Khoản vay qua ứng dụng có thêm hướng dẫn riêng")
    void appLoanAddsGuidance() {
        RateCheckRequest r = new RateCheckRequest(
                new BigDecimal("10"), new BigDecimal("10000000"), 12, true);

        RateCheckResponse res = service.checkRate(1L, r);

        assertThat(res.guidance()).contains("ứng dụng");
    }
}
