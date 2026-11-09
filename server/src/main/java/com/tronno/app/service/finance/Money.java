package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money
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

public final class Money {
    public static final int SCALE = 2;

    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static final int RATE_SCALE = 8;

    private Money() {
    }

    /**
     * Chuyển đổi một giá trị bất kỳ sang BigDecimal đã chuẩn hoá 2 chữ số thập phân.
     *
     * @param value giá trị cần chuyển đổi, có thể null
     * @return BigDecimal giá trị đã chuẩn hoá
     */
    public static BigDecimal of(Object value) {
        if (value == null) return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        if (value instanceof BigDecimal bd) return bd.setScale(SCALE, ROUNDING);
        return new BigDecimal(value.toString()).setScale(SCALE, ROUNDING);
    }

    /**
     * Trả về giá trị 0 đã chuẩn hoá 2 chữ số thập phân.
     *
     * @return BigDecimal bằng 0
     */
    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
    }

    /**
     * Làm tròn số tiền về 2 chữ số thập phân theo quy tắc HALF_UP.
     *
     * @param value số tiền cần làm tròn
     * @return BigDecimal đã làm tròn
     */
    public static BigDecimal round(BigDecimal value) {
        return value == null ? zero() : value.setScale(SCALE, ROUNDING);
    }

    /**
     * Làm tròn lãi suất về 8 chữ số thập phân để tránh sai số khi tính.
     *
     * @param value lãi suất cần làm tròn
     * @return BigDecimal đã làm tròn
     */
    public static BigDecimal roundRate(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(RATE_SCALE, ROUNDING) : value.setScale(RATE_SCALE, ROUNDING);
    }

    /**
     * Cộng hai số tiền.
     *
     * @param a số tiền thứ nhất
     * @param b số tiền thứ hai
     * @return BigDecimal tổng đã làm tròn
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return round(nz(a).add(nz(b)));
    }

    /**
     * Trừ hai số tiền.
     *
     * @param a số tiền bị trừ
     * @param b số tiền trừ đi
     * @return BigDecimal hiệu đã làm tròn
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return round(nz(a).subtract(nz(b)));
    }

    /**
     * Nhân hai số tiền.
     *
     * @param a số tiền thứ nhất
     * @param b số tiền thứ hai
     * @return BigDecimal tích đã làm tròn
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return round(nz(a).multiply(nz(b)));
    }

    /**
     * Chia hai số tiền, trả về 0 nếu mẫu số bằng 0.
     *
     * @param a số tiền bị chia
     * @param b mẫu số
     * @return BigDecimal thương đã làm tròn
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (nz(b).signum() == 0) return zero();
        return nz(a).divide(nz(b), SCALE, ROUNDING);
    }

    /**
     * Lấy giá trị lớn hơn trong hai số tiền.
     *
     * @param a số tiền thứ nhất
     * @param b số tiền thứ hai
     * @return BigDecimal giá trị lớn hơn
     */
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b)) >= 0 ? nz(a) : nz(b);
    }

    /**
     * Lấy giá trị nhỏ hơn trong hai số tiền.
     *
     * @param a số tiền thứ nhất
     * @param b số tiền thứ hai
     * @return BigDecimal giá trị nhỏ hơn
     */
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b)) <= 0 ? nz(a) : nz(b);
    }

    /**
     * Kiểm tra số tiền có bằng 0 hay không.
     *
     * @param a số tiền cần kiểm tra
     * @return boolean true nếu bằng 0
     */
    public static boolean isZero(BigDecimal a) {
        return nz(a).signum() == 0;
    }

    /**
     * Kiểm tra số tiền có lớn hơn 0 hay không.
     *
     * @param a số tiền cần kiểm tra
     * @return boolean true nếu lớn hơn 0
     */
    public static boolean isPositive(BigDecimal a) {
        return nz(a).signum() > 0;
    }

    /**
     * Kiểm tra số tiền có nhỏ hơn 0 hay không.
     *
     * @param a số tiền cần kiểm tra
     * @return boolean true nếu nhỏ hơn 0
     */
    public static boolean isNegative(BigDecimal a) {
        return nz(a).signum() < 0;
    }

    /**
     * So sánh hai số tiền.
     *
     * @param a số tiền thứ nhất
     * @param b số tiền thứ hai
     * @return int âm nếu a nhỏ hơn b, 0 nếu bằng, dương nếu a lớn hơn
     */
    public static int compareTo(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b));
    }

    /**
     * Quy đổi null thành 0 để thuận tiện khi tính toán.
     *
     * @param value số tiền có thể null
     * @return BigDecimal không null
     */
    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
