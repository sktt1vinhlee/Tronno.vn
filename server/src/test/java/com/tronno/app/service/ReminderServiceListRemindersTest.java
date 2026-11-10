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

import com.tronno.app.models.Reminder;
import com.tronno.app.models.ReminderStatus;
import com.tronno.app.models.ReminderType;
import com.tronno.app.repository.DebtRepository;
import com.tronno.app.repository.ReminderRepository;
import com.tronno.app.repository.RepaymentScheduleRepository;
import com.tronno.app.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.ReminderServiceTestData.TODAY;
import static com.tronno.app.service.ReminderServiceTestData.debt;

/**
 * ReminderServiceListRemindersTest
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
@DisplayName("ReminderService.list() - Danh sách nhắc hạn")
public class ReminderServiceListRemindersTest {

    @Mock
    private ReminderRepository reminders;

    @Mock
    private DebtRepository debts;

    @Mock
    private RepaymentScheduleRepository schedules;

    @Mock
    private UserRepository users;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ReminderService service;

    @Test
    @DisplayName("Trả về nhắc hạn kèm tên khoản nợ")
    void returnsRemindersWithDebtName() {
        Reminder r = Reminder.builder()
                .id(1L)
                .userId(1L)
                .debtId(10L)
                .scheduleId(1L)
                .remindType(ReminderType.ON_DUE)
                .channel("EMAIL")
                .remindAt(TODAY)
                .priority(10)
                .status(ReminderStatus.SENT)
                .message("Nội dung")
                .build();

        when(reminders.findByUserIdOrderByRemindAtAsc(1L)).thenReturn(List.of(r));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));

        var result = service.list(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).debtName()).isEqualTo("Vay mua xe");
        assertThat(result.get(0).remindType()).isEqualTo(ReminderType.ON_DUE);
    }

    @Test
    @DisplayName("Khoản nợ đã bị xoá thì tên trả về null")
    void returnsNullDebtNameWhenDebtMissing() {
        Reminder r = Reminder.builder()
                .id(1L)
                .userId(1L)
                .debtId(99L)
                .remindType(ReminderType.ON_DUE)
                .remindAt(LocalDate.of(2026, 11, 10))
                .build();

        when(reminders.findByUserIdOrderByRemindAtAsc(1L)).thenReturn(List.of(r));
        when(debts.findById(99L)).thenReturn(Optional.empty());

        var result = service.list(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).debtName()).isNull();
    }
}
