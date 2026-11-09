package com.tronno.app.models;

/**
 * Kiểu thời hạn trả của khoản nợ.
 */
public enum TermType {
    /** Người dùng chọn ngày đáo hạn cụ thể, hệ thống chia lịch trả theo tháng. */
    FIXED_DATE,
    /** Trả theo quý: mỗi kỳ 3 tháng một lần. */
    QUARTERLY,
}
