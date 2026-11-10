package com.tronno.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
import com.tronno.app.models.Reminder;
import com.tronno.app.models.ReminderStatus;
import com.tronno.app.models.ReminderType;
import com.tronno.app.models.RepaymentSchedule;
import com.tronno.app.models.User;
import com.tronno.app.repository.DebtRepository;
import com.tronno.app.repository.ReminderRepository;
import com.tronno.app.repository.RepaymentScheduleRepository;
import com.tronno.app.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.tronno.app.service.ReminderServiceTestData.TODAY;
import static com.tronno.app.service.ReminderServiceTestData.debt;
import static com.tronno.app.service.ReminderServiceTestData.schedule;
import static com.tronno.app.service.ReminderServiceTestData.user;

/**
 * ReminderServiceSendDueRemindersTest
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
@DisplayName("ReminderService.sendDueReminders() - Sinh và gửi nhắc hạn")
public class ReminderServiceSendDueRemindersTest {

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
    @DisplayName("Nhắc kỳ trả sắp tới hạn với loại BEFORE_DUE")
    void sendsBeforeDueReminder() {
        RepaymentSchedule s = schedule(1L, 1, TODAY.plusDays(3));

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));
        when(users.findById(1L)).thenReturn(Optional.of(user()));
        when(reminders.existsByScheduleIdAndNotifyDate(1L, TODAY)).thenReturn(false);
        when(mailService.sendReminderMail(any(), any(), any())).thenReturn(true);

        int sent = service.sendDueReminders(TODAY);

        assertThat(sent).isEqualTo(1);

        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminders, org.mockito.Mockito.atLeastOnce()).save(captor.capture());

        Reminder saved = captor.getAllValues().get(0);
        assertThat(saved.getRemindType()).isEqualTo(ReminderType.BEFORE_DUE);
        assertThat(saved.getNotifyDate()).isEqualTo(TODAY);
        assertThat(saved.getChannel()).isEqualTo("EMAIL");
    }

    @Test
    @DisplayName("Nhắc kỳ trả đến hạn hôm nay với loại ON_DUE")
    void sendsOnDueReminder() {
        RepaymentSchedule s = schedule(1L, 1, TODAY);

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));
        when(users.findById(1L)).thenReturn(Optional.of(user()));
        when(reminders.existsByScheduleIdAndNotifyDate(1L, TODAY)).thenReturn(false);
        when(mailService.sendReminderMail(any(), any(), any())).thenReturn(true);

        int sent = service.sendDueReminders(TODAY);

        assertThat(sent).isEqualTo(1);

        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminders, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getRemindType()).isEqualTo(ReminderType.ON_DUE);
    }

    @Test
    @DisplayName("Nhắc kỳ trả quá hạn với loại OVERDUE")
    void sendsOverdueReminder() {
        RepaymentSchedule s = schedule(1L, 1, TODAY.minusDays(7));

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));
        when(users.findById(1L)).thenReturn(Optional.of(user()));
        when(reminders.existsByScheduleIdAndNotifyDate(1L, TODAY)).thenReturn(false);
        when(mailService.sendReminderMail(any(), any(), any())).thenReturn(true);

        int sent = service.sendDueReminders(TODAY);

        assertThat(sent).isEqualTo(1);

        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminders, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getRemindType()).isEqualTo(ReminderType.OVERDUE);
    }

    @Test
    @DisplayName("Không gửi lại khi kỳ trả đã được nhắc trong ngày")
    void skipsAlreadyNotifiedSchedule() {
        RepaymentSchedule s = schedule(1L, 1, TODAY);

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));
        when(users.findById(1L)).thenReturn(Optional.of(user()));
        when(reminders.existsByScheduleIdAndNotifyDate(1L, TODAY)).thenReturn(true);

        int sent = service.sendDueReminders(TODAY);

        assertThat(sent).isZero();
        verify(reminders, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Bỏ qua khoản nợ không còn hiệu lực")
    void skipsInactiveDebt() {
        Debt d = debt();
        d.setStatus(DebtStatus.PAID);
        RepaymentSchedule s = schedule(1L, 1, TODAY);

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(d));

        int sent = service.sendDueReminders(TODAY);

        assertThat(sent).isZero();
        verify(reminders, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Bỏ qua người dùng bị khoá")
    void skipsBlockedUser() {
        User blocked = user();
        blocked.setBlocked(true);
        RepaymentSchedule s = schedule(1L, 1, TODAY);

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));
        when(users.findById(1L)).thenReturn(Optional.of(blocked));

        int sent = service.sendDueReminders(TODAY);

        assertThat(sent).isZero();
        verify(reminders, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Gửi email tới chính email của người đi nợ")
    void sendsMailToBorrowerOwnEmail() {
        RepaymentSchedule s = schedule(1L, 1, TODAY);

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));
        when(users.findById(1L)).thenReturn(Optional.of(user()));
        when(reminders.existsByScheduleIdAndNotifyDate(1L, TODAY)).thenReturn(false);
        when(mailService.sendReminderMail(any(), any(), any())).thenReturn(true);

        service.sendDueReminders(TODAY);

        verify(mailService).sendReminderMail(eq("vinh@example.com"), any(), any());
    }

    @Test
    @DisplayName("Đánh dấu FAILED khi gửi email thất bại")
    void marksFailedWhenMailFails() {
        RepaymentSchedule s = schedule(1L, 1, TODAY);

        when(schedules.findOpenSchedulesDueBefore(any())).thenReturn(List.of(s));
        when(debts.findById(10L)).thenReturn(Optional.of(debt()));
        when(users.findById(1L)).thenReturn(Optional.of(user()));
        when(reminders.existsByScheduleIdAndNotifyDate(1L, TODAY)).thenReturn(false);
        when(mailService.sendReminderMail(any(), any(), any())).thenReturn(false);

        int sent = service.sendDueReminders(TODAY);

        assertThat(sent).isZero();

        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminders, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getStatus()).isEqualTo(ReminderStatus.FAILED);
    }

    @Test
    @DisplayName("Chỉ nhắc kỳ trả tới hạn trong 5 ngày và các kỳ đã quá hạn")
    void usesFiveDayHorizon() {
        when(schedules.findOpenSchedulesDueBefore(any(LocalDate.class)))
                .thenReturn(List.of());

        service.sendDueReminders(TODAY);

        verify(schedules).findOpenSchedulesDueBefore(eq(TODAY.plusDays(5)));
    }
}
