package com.tronno.app.validation;

/**
 * EmailSample
 *
 * <p>Kiểu dữ liệu mẫu dùng để kiểm tra ràng buộc {@link ValidEmail}.</p>
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
 *
 * @param email giá trị email cần kiểm tra
 */
public record EmailSample(@ValidEmail String email) {
}
