package com.tronno.app.config;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tronno.app.models.User;
import com.tronno.app.models.UserRole;
import com.tronno.app.repository.UserRepository;

/**
 * Khởi tạo dữ liệu nền tối thiểu.
 * <p>QUAN TRỌNG: KHÔNG seed khoản nợ. Người dùng tự nhập tay từng khoản nợ
 * (người cần trả, số tiền, kỳ hạn, cách tính, phí, ghi chú) trên giao diện.
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
@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "123456aA@";

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Bean
    @Order(1)
    ApplicationRunner seedData() {
        return args -> {
            seedUser("member@tronno.vn", "Người Dùng", UserRole.MEMBER, "0901000001",
                    new BigDecimal("15000000"));
            seedUser("admin@tronno.vn", "Quản Trị Viên", UserRole.ADMIN, "0901000002", BigDecimal.ZERO);
        };
    }

    /**
     * Tạo tài khoản mẫu nếu chưa tồn tại.
     *
     * @param email email tài khoản
     * @param fullName họ tên hiển thị
     * @param role vai trò
     * @param phone số điện thoại
     * @param monthlyIncome thu nhập hằng tháng
     * @return User entity người dùng
     */
    private User seedUser(String email, String fullName, UserRole role, String phone,
            BigDecimal monthlyIncome) {
        return users.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setFullName(fullName);
            u.setPhone(phone);
            u.setRole(role);
            u.setMonthlyIncome(monthlyIncome);
            u.setPassword(encoder.encode(DEFAULT_PASSWORD));
            users.save(u);
            log.info("Seeded demo account: {} ({})", email, role);
            return u;
        });
    }
}
