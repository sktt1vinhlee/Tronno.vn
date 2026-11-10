package com.tronno.app.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.dtos.DebtDtos.PaymentRequest;
import com.tronno.app.exception.ResourceNotFoundException;
import com.tronno.app.models.Debt;
import com.tronno.app.models.Payment;
import com.tronno.app.models.PaymentAllocation;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.repository.CreditorRepository;
import com.tronno.app.repository.DebtRepository;
import com.tronno.app.repository.InterestRateHistoryRepository;
import com.tronno.app.repository.PaymentAllocationRepository;
import com.tronno.app.repository.PaymentRepository;
import com.tronno.app.repository.ReminderRepository;
import com.tronno.app.repository.RepaymentScheduleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.DebtServiceTestData.DEBT_ID;
import static com.tronno.app.service.DebtServiceTestData.TODAY;
import static com.tronno.app.service.DebtServiceTestData.USER_ID;
import static com.tronno.app.service.DebtServiceTestData.debt;
import static com.tronno.app.service.DebtServiceTestData.schedule;

/**
 * DebtServiceRecordPaymentTest
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
@DisplayName("DebtService.recordPayment() - Ghi nhận thanh toán")
public class DebtServiceRecordPaymentTest {

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
    @DisplayName("Ghi nhận khoản trả và phân bổ vào kỳ trả")
    void recordsPayment() {
        RepaymentSchedule s = schedule(1L, 1, TODAY, "1000000.00");
        List<RepaymentSchedule> open = new ArrayList<>(List.of(s));

        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdAndStatusInOrderByInstallmentNoAsc(anyLong(), any()))
                .thenReturn(open);
        when(payments.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });
        when(schedules.findById(1L)).thenReturn(Optional.of(s));
        when(schedules.save(any(RepaymentSchedule.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(DEBT_ID)).thenReturn(List.of(s));

        PaymentRequest r = new PaymentRequest(new BigDecimal("500000"), TODAY, "CASH", null, null);
        var res = service.recordPayment(USER_ID, DEBT_ID, r);

        assertThat(res.amount()).isEqualByComparingTo("500000");
        assertThat(res.appliedAmount()).isEqualByComparingTo("500000");
        assertThat(res.surplus()).isEqualByComparingTo("0.00");
        assertThat(res.allocations()).hasSize(1);
        assertThat(res.allocations().get(0).component()).isEqualTo("PRINCIPAL");

        verify(allocations).save(any(PaymentAllocation.class));
    }

    @Test
    @DisplayName("Trả vượt số tiền còn nợ thì phần dư được ghi nhận")
    void excessPaymentCreatesSurplus() {
        RepaymentSchedule s = schedule(1L, 1, TODAY, "1000000.00");

        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdAndStatusInOrderByInstallmentNoAsc(anyLong(), any()))
                .thenReturn(List.of(s));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });
        when(schedules.findById(1L)).thenReturn(Optional.of(s));
        when(schedules.save(any(RepaymentSchedule.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(DEBT_ID)).thenReturn(List.of(s));

        PaymentRequest r = new PaymentRequest(new BigDecimal("1500000"), TODAY, "CASH", null, null);
        var res = service.recordPayment(USER_ID, DEBT_ID, r);

        assertThat(res.appliedAmount()).isEqualByComparingTo("1000000.00");
        assertThat(res.surplus()).isEqualByComparingTo("500000.00");
    }

    @Test
    @DisplayName("Khoản nợ không tồn tại thì báo lỗi 404")
    void unknownDebtThrows() {
        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.empty());

        PaymentRequest r = new PaymentRequest(new BigDecimal("100000"), TODAY, "CASH", null, null);

        assertThatThrownBy(() -> service.recordPayment(USER_ID, DEBT_ID, r))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
