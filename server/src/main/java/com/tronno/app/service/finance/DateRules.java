package com.tronno.app.service.finance;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateRules {
    private DateRules() {
    }

    public static LocalDate addMonths(LocalDate date, int months) {
        return date.plusMonths(months);
    }

    /** Cộng số tháng theo quý (mỗi quý = 3 tháng). */
    public static LocalDate addQuarters(LocalDate date, int quarters) {
        return date.plusMonths(quarters * 3L);
    }

    public static LocalDate endOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static long daysInclusive(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) return 0;
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Dời ngày đến hạn sang ngày làm việc tiếp theo nếu rơi vào cuối tuần.
     */
    public static LocalDate adjustToBusinessDay(LocalDate date) {
        LocalDate result = date;

        for (int i = 0; i < 10; i++) {
            if (!isWeekend(result)) break;
            result = result.plusDays(1);
        }
        return result;
    }

    public static LocalDate startOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate lastDayOfMonth(LocalDate date) {
        return endOfMonth(date);
    }
}
