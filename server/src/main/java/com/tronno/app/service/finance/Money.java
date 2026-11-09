package com.tronno.app.service.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    public static final int SCALE = 2;

    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static final int RATE_SCALE = 8;

    private Money() {
    }

    public static BigDecimal of(Object value) {
        if (value == null) return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        if (value instanceof BigDecimal bd) return bd.setScale(SCALE, ROUNDING);
        return new BigDecimal(value.toString()).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal round(BigDecimal value) {
        return value == null ? zero() : value.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal roundRate(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(RATE_SCALE, ROUNDING) : value.setScale(RATE_SCALE, ROUNDING);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return round(nz(a).add(nz(b)));
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return round(nz(a).subtract(nz(b)));
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return round(nz(a).multiply(nz(b)));
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (nz(b).signum() == 0) return zero();
        return nz(a).divide(nz(b), SCALE, ROUNDING);
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b)) >= 0 ? nz(a) : nz(b);
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b)) <= 0 ? nz(a) : nz(b);
    }

    public static boolean isZero(BigDecimal a) {
        return nz(a).signum() == 0;
    }

    public static boolean isPositive(BigDecimal a) {
        return nz(a).signum() > 0;
    }

    public static boolean isNegative(BigDecimal a) {
        return nz(a).signum() < 0;
    }

    public static int compareTo(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b));
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
