package com.tronno.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Kiểm tra schema đã được tạo hay chưa.
 * <p>TRONNO không dùng Flyway. Schema được tạo bằng file SQL thủ công:
 * {@code src/main/resources/db/01_schema.sql}.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SchemaGuard {
    private static final List<String> REQUIRED_TABLES = List.of(
            "users", "creditors", "debts", "repayment_schedules", "interest_rate_history",
            "payments", "payment_allocations", "reminders");

    private final JdbcTemplate jdbc;

    @Bean
    @Order(0)
    ApplicationRunner verifySchema() {
        return args -> {
            List<String> existing = jdbc.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                    String.class);
            List<String> missing = REQUIRED_TABLES.stream()
                    .filter(t -> !existing.contains(t))
                    .toList();

            if (!missing.isEmpty()) {
                log.error("""

                        ============================================================================
                        THIẾU BẢNG TRONG DATABASE: {}
                        Hãy chạy file schema trước khi khởi động ứng dụng:
                          cmd /c "mysql -uroot -p < server/src/main/resources/db/01_schema.sql"
                        ============================================================================
                        """, missing);
                throw new IllegalStateException("Thiếu bảng: " + missing);
            }
            log.info("Schema OK: {} bảng đã sẵn sàng.", REQUIRED_TABLES.size());
        };
    }
}
