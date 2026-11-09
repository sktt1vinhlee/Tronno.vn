package com.tronno.app.models;

/**
 * Mức nhắc hạn tự động.
 * TRONNO tự sinh nhắc mỗi ngày cho tới khi khoản nợ được thanh toán.
 */
public enum ReminderType {
    /** Còn <= 5 ngày tới hạn: nhắc trước. */
    BEFORE_DUE,
    /** Đến hạn hôm nay. */
    ON_DUE,
    /** Đã quá hạn: nhắc mỗi ngày tới khi khoản nợ được trả. */
    OVERDUE,
    /** Nhắc tổng hợp hằng ngày. */
    DAILY
}
