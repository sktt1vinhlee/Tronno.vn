package com.tronno.app.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.repository.CreditorRepository;
import com.tronno.app.repository.DebtRepository;
import com.tronno.app.repository.InterestRateHistoryRepository;
import com.tronno.app.repository.PaymentAllocationRepository;
import com.tronno.app.repository.PaymentRepository;
import com.tronno.app.repository.ReminderRepository;
import com.tronno.app.repository.RepaymentScheduleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.DebtServiceTestData.DEBT_ID;
import static com.tronno.app.service.DebtServiceTestData.TODAY;
import static com.tronno.app.service.DebtServiceTestData.USER_ID;
import static com.tronno.app.service.DebtServiceTestData.debt;
import static com.tronno.app.service.DebtServiceTestData.schedule;

/**
 * DebtServiceListScheduleTest
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
@DisplayName("DebtService.listSchedule() - Lịch trả của khoản nợ")
public class DebtServiceListScheduleTest {

    @Mock
    private DebtRepository debts;

    @Mock
    private CreditorRepository creditors;

    @Mock
    private RepaymentScheduleRepository schedules;

    @Mock
    private PaymentRepository payments;

    @Mock
    private PaymentAllocationRepository allocations;

    @Mock
    private InterestRateHistoryRepository rateHistory;

    @Mock
    private ReminderRepository reminders;

    @InjectMocks
    private DebtService service;

    @Test
    @DisplayName("Trả về lịch trả kèm số tiền còn lại")
    void returnsScheduleWithRemaining() {
        RepaymentSchedule s = schedule(1L, 1, TODAY, "1000000.00");
        s.setPaidAmount(new BigDecimal("200000.00"));

        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(DEBT_ID)).thenReturn(List.of(s));

        var res = service.listSchedule(USER_ID, DEBT_ID);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).remaining()).isEqualByComparingTo("800000.00");
    }
}
