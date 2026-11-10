package com.tronno.app.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.when;
import static com.tronno.app.service.DebtServiceTestData.DEBT_ID;
import static com.tronno.app.service.DebtServiceTestData.USER_ID;
import static com.tronno.app.service.DebtServiceTestData.debt;

/**
 * DebtServiceQueryTest
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
@DisplayName("DebtService.getDebt()/listDebts() - Truy vấn khoản nợ")
class DebtServiceQueryTest {

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
    @DisplayName("Lấy chi tiết khoản nợ của chính người dùng")
    void getsOwnDebt() {
        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.of(debt()));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(DEBT_ID)).thenReturn(List.of());

        var res = service.getDebt(USER_ID, DEBT_ID);

        assertThat(res.id()).isEqualTo(DEBT_ID);
        assertThat(res.name()).isEqualTo("Vay mua xe máy");
        assertThat(res.remainingPrincipal()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Khoản nợ không thuộc về người dùng thì báo lỗi 404")
    void otherUsersDebtThrows() {
        when(debts.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDebt(USER_ID, DEBT_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Danh sách khoản nợ được lọc theo trạng thái")
    void listsDebtsByStatus() {
        when(debts.findByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, DebtStatus.ACTIVE))
                .thenReturn(List.of(debt()));
        when(schedules.findByDebtIdOrderByInstallmentNoAsc(DEBT_ID)).thenReturn(List.of());

        var res = service.listDebts(USER_ID, DebtStatus.ACTIVE);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).id()).isEqualTo(DEBT_ID);
    }
}
