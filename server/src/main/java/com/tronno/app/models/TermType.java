package com.tronno.app.models;

/**
 * Kiểu thời hạn trả của khoản nợ.
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

public enum TermType {
    /** Người dùng chọn ngày đáo hạn cụ thể, hệ thống chia lịch trả theo tháng. */
    FIXED_DATE,
    /** Trả theo quý: mỗi kỳ 3 tháng một lần. */
    QUARTERLY,
}
