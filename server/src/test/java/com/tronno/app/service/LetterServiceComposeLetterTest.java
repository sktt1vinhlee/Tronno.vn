package com.tronno.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.dtos.TrickDtos.LetterRequest;
import com.tronno.app.models.Debt;
import com.tronno.app.repository.DebtRepository;
import com.tronno.app.repository.RepaymentScheduleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.LetterServiceTestData.debt;
import static com.tronno.app.service.LetterServiceTestData.schedule;

/**
 * LetterServiceComposeLetterTest
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
@DisplayName("LetterService.composeLetter() - Soạn nội dung thư")
public class LetterServiceComposeLetterTest {

    @Mock
    private DebtRepository debts;

    @Mock
    private RepaymentScheduleRepository schedules;

    @InjectMocks
    private LetterService service;

    @Test
    @DisplayName("Giọng văn LIGHT tạo tiêu đề nhẹ nhàng")
    void lightToneTitle() {
        when(debts.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(10L)).thenReturn(List.of());

        var res = service.composeLetter(1L, new LetterRequest(10L, "LIGHT"));

        assertThat(res.title()).contains("nhẹ nhàng");
    }

    @Test
    @DisplayName("Giọng văn HARD tạo tiêu đề dứt khoát")
    void hardToneTitle() {
        when(debts.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(10L)).thenReturn(List.of());

        var res = service.composeLetter(1L, new LetterRequest(10L, "HARD"));

        assertThat(res.title()).contains("dứt khoát");
    }

    @Test
    @DisplayName("Giọng văn MEDIUM là mặc định")
    void mediumIsDefaultTone() {
        when(debts.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(10L)).thenReturn(List.of());

        var res = service.composeLetter(1L, new LetterRequest(10L, null));

        assertThat(res.title()).contains("gia hạn");
    }

    @Test
    @DisplayName("Nội dung thư chứa số dư còn lại của khoản nợ")
    void bodyContainsRemainingAmount() {
        when(debts.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(10L)).thenReturn(List.of(
                schedule(LocalDate.of(2026, 2, 1), "1000000.00", "200000.00")));

        var res = service.composeLetter(1L, new LetterRequest(10L, "MEDIUM"));

        assertThat(res.body()).contains("800000.00");
        assertThat(res.body()).contains("Vay mua xe máy");
    }

    @Test
    @DisplayName("Không có khoản nợ vẫn soạn được thư với giá trị mặc định")
    void composesWithoutDebt() {
        var res = service.composeLetter(1L, new LetterRequest(null, "MEDIUM"));

        assertThat(res.body()).contains("quý khách");
        assertThat(res.disclaimer()).isNotBlank();
    }
}
