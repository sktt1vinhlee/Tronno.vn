package com.tronno.app.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.dtos.DebtDtos.DebtRequest;
import com.tronno.app.models.Creditor;
import com.tronno.app.models.CreditorKind;
import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.models.TermType;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.DebtServiceTestData.DEBT_ID;
import static com.tronno.app.service.DebtServiceTestData.USER_ID;
import static com.tronno.app.service.DebtServiceTestData.request;

/**
 * DebtServiceCreateDebtTest
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
@DisplayName("DebtService.createDebt() - Tạo khoản nợ")
public class DebtServiceCreateDebtTest {

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
    @DisplayName("Tạo khoản nợ kèm lịch trả và lịch sử lãi suất")
    void createsDebtWithSchedule() {
        when(debts.save(any(Debt.class))).thenAnswer(inv -> {
            Debt d = inv.getArgument(0);
            d.setId(DEBT_ID);
            return d;
        });
        when(schedules.saveAll(any(List.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.createDebt(USER_ID, request("Vay mua xe", "12000000", 12).build());

        assertThat(res.name()).isEqualTo("Vay mua xe");
        assertThat(res.principal()).isEqualByComparingTo("12000000");
        assertThat(res.termMonths()).isEqualTo(12);
        assertThat(res.status()).isEqualTo(DebtStatus.ACTIVE);

        verify(schedules).saveAll(any(List.class));
        verify(rateHistory).save(any());
    }

    @Test
    @DisplayName("Nhập tay tên chủ nợ thì tự tạo chủ nợ mới")
    void createsCreditorFromTypedName() {
        DebtRequest r = request("Vay mua xe", "5000000", 12)
                .creditorName("Nguyễn Văn A")
                .creditorKind(CreditorKind.PERSON)
                .creditorPhone("0900000000")
                .creditorEmail("a@example.com")
                .build();

        when(creditors.save(any(Creditor.class))).thenAnswer(inv -> {
            Creditor c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });
        when(debts.save(any(Debt.class))).thenAnswer(inv -> {
            Debt d = inv.getArgument(0);
            d.setId(DEBT_ID);
            return d;
        });
        when(schedules.saveAll(any(List.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createDebt(USER_ID, r);

        ArgumentCaptor<Creditor> captor = ArgumentCaptor.forClass(Creditor.class);
        verify(creditors).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Nguyễn Văn A");
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("Ngày đáo hạn trước ngày giải ngân thì báo lỗi nghiệp vụ")
    void maturityBeforeDisbursementFails() {
        DebtRequest r = request("Vay mua xe", "5000000", 12)
                .disbursementDate(LocalDate.of(2026, 6, 1))
                .maturityDate(LocalDate.of(2026, 1, 1))
                .build();

        assertThatThrownBy(() -> service.createDebt(USER_ID, r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ngày đáo hạn");
    }

    @Test
    @DisplayName("Kỳ hạn theo quý được chuẩn hoá về bội số của 3 tháng")
    void quarterlyTermIsNormalized() {
        DebtRequest r = request("Vay theo quý", "12000000", 10)
                .termType(TermType.QUARTERLY)
                .build();

        when(debts.save(any(Debt.class))).thenAnswer(inv -> {
            Debt d = inv.getArgument(0);
            d.setId(DEBT_ID);
            return d;
        });
        when(schedules.saveAll(any(List.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.createDebt(USER_ID, r);

        // 10 tháng -> làm tròn thành 3 quý -> 9 tháng.
        assertThat(res.termMonths()).isEqualTo(9);
        assertThat(res.termType()).isEqualTo(TermType.QUARTERLY);
    }
}
