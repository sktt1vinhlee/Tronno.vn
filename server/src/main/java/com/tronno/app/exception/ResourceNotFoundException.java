package com.tronno.app.exception;
/**
 * ResourceNotFoundException
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

public class ResourceNotFoundException extends RuntimeException {
    /**
     * Tạo lỗi không tìm thấy tài nguyên.
     *
     * @param message thông báo lỗi
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
