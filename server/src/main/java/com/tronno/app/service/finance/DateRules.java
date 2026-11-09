package com.tronno.app.service.finance;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DateRules
 *
 * Version 1.0
 *
 * Date: 09-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 09-11-2026         VinhHL             Create
 */

public final class DateRules {
    private DateRules() {
    }

    /**
     * Cộng số tháng vào một ngày.
     *
     * @param date ngày bắt đầu
     * @param months số tháng cần cộng
     * @return LocalDate ngày mới
     */
    public static LocalDate addMonths(LocalDate date, int months) {
        return date.plusMonths(months);
    }

    /**
     * Cộng số quý vào một ngày, mỗi quý tương ứng 3 tháng.
     *
     * @param date ngày bắt đầu
     * @param quarters số quý cần cộng
     * @return LocalDate ngày mới
     */
    public static LocalDate addQuarters(LocalDate date, int quarters) {
        return date.plusMonths(quarters * 3L);
    }

    /**
     * Lấy ngày cuối cùng của tháng chứa ngày cho trước.
     *
     * @param date ngày bất kỳ
     * @return LocalDate ngày cuối tháng
     */
    public static LocalDate endOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Đếm số ngày tính cả hai đầu mốc.
     *
     * @param start ngày bắt đầu
     * @param end ngày kết thúc
     * @return long số ngày, tối thiểu 0
     */
    public static long daysInclusive(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) return 0;
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    /**
     * Đếm số ngày giữa hai mốc, không tính ngày kết thúc.
     *
     * @param start ngày bắt đầu
     * @param end ngày kết thúc
     * @return long số ngày
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Kiểm tra ngày có rơi vào cuối tuần hay không.
     *
     * @param date ngày cần kiểm tra
     * @return boolean true nếu là cuối tuần
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Dời ngày đến hạn sang ngày làm việc tiếp theo nếu rơi vào cuối tuần.
     *
     * @param date ngày đến hạn
     * @return LocalDate ngày làm việc
     */
    public static LocalDate adjustToBusinessDay(LocalDate date) {
        LocalDate result = date;

        for (int i = 0; i < 10; i++) {
            if (!isWeekend(result)) break;
            result = result.plusDays(1);
        }
        return result;
    }

    /**
     * Lấy ngày đầu tiên của tháng chứa ngày cho trước.
     *
     * @param date ngày bất kỳ
     * @return LocalDate ngày đầu tháng
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * Lấy ngày cuối cùng của tháng chứa ngày cho trước.
     *
     * @param date ngày bất kỳ
     * @return LocalDate ngày cuối tháng
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {
        return endOfMonth(date);
    }
}
