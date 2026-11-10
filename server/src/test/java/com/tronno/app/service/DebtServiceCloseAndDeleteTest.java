package com.tronno.app.service;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.DebtServiceTestData.DEBT_ID;
import static com.tronno.app.service.DebtServiceTestData.USER_ID;
import static com.tronno.app.service.DebtServiceTestData.debt;

/**
 * DebtServiceCloseAndDeleteTest
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
@DisplayName("DebtService.closeDebt()/deleteDebt() - Đóng và xoá khoản nợ")
public class DebtServiceCloseAndDeleteTest {

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
    @DisplayName("Chuyển khoản nợ sang trạng thái đã tất toán")
    void closesDebt() {
        Debt d = debt();
        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.of(d));
        when(debts.save(any(Debt.class))).thenAnswer(inv -> inv.getArgument(0));

        service.closeDebt(USER_ID, DEBT_ID, DebtStatus.PAID);

        assertThat(d.getStatus()).isEqualTo(DebtStatus.PAID);
        verify(debts).save(any(Debt.class));
    }

    @Test
    @DisplayName("Không cho xoá khoản nợ đang còn hiệu lực")
    void cannotDeleteActiveDebt() {
        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.of(debt()));

        assertThatThrownBy(() -> service.deleteDebt(USER_ID, DEBT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("còn hiệu lực");

        verify(debts, never()).delete(any(Debt.class));
    }

    @Test
    @DisplayName("Xoá khoản nợ đã đóng kèm lịch trả và nhắc hạn")
    void deletesClosedDebt() {
        Debt d = debt();
        d.setStatus(DebtStatus.PAID);
        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.of(d));

        service.deleteDebt(USER_ID, DEBT_ID);

        verify(reminders).deleteByDebtId(DEBT_ID);
        verify(schedules).deleteByDebtId(DEBT_ID);
        verify(debts).delete(d);
    }
}
