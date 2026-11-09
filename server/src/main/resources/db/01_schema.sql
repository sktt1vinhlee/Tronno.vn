-- ============================================================================
-- TRONNO - SCHEMA DATABASE (MySQL 8.0)
-- Chạy file này 1 LẦN DUY NHẤT để tạo cấu trúc bảng.
-- Không seed dữ liệu: người dùng tự nhập tay khoản nợ trên giao diện.
--
-- Cách chạy (PowerShell):
--   cmd /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p < 01_schema.sql'
-- Hoặc trong MySQL Workbench: File > Open > 01_schema.sql > Execute
-- ============================================================================

CREATE DATABASE IF NOT EXISTS tronno
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tronno;

-- ---------------------------------------------------------------------------
-- users: tài khoản người dùng
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    email          VARCHAR(190) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    full_name      VARCHAR(150) NOT NULL,
    phone          VARCHAR(30),
    address        VARCHAR(500),
    avatar_url     VARCHAR(1000),
    role           VARCHAR(32)   NOT NULL DEFAULT 'MEMBER',
    monthly_income DECIMAL(18,2) NOT NULL DEFAULT 0,
    blocked        TINYINT(1)    NOT NULL DEFAULT 0,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------
-- creditors: chủ nợ (người/ngân hàng mà bạn nợ)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS creditors (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    name       VARCHAR(150) NOT NULL,
    kind       VARCHAR(32)  NOT NULL DEFAULT 'PERSON',
    phone      VARCHAR(30),
    email      VARCHAR(190),
    note       VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_creditors_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------
-- debts: khoản nợ bạn phải trả
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS debts (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT NOT NULL,
    creditor_id        BIGINT,
    name               VARCHAR(150) NOT NULL,
    debt_type          VARCHAR(32)  NOT NULL,
    principal          DECIMAL(18,2) NOT NULL,
    annual_rate        DECIMAL(9,4)  NOT NULL DEFAULT 0,
    interest_method    VARCHAR(24)  NOT NULL DEFAULT 'REDUCING',
    schedule_type      VARCHAR(24)  NOT NULL DEFAULT 'EQUAL_PRINCIPAL',
    disbursement_date  DATE NOT NULL,
    term_months        INT NOT NULL,
    term_type          VARCHAR(24)  NOT NULL DEFAULT 'FIXED_DATE',
    maturity_date      DATE NULL,
    installment_amount DECIMAL(18,2) NULL,
    late_fee_type      VARCHAR(24)  NOT NULL DEFAULT 'NONE',
    late_fee_rate      DECIMAL(12,8) NOT NULL DEFAULT 0,
    late_fee_amount    DECIMAL(18,2) NOT NULL DEFAULT 0,
    early_fee_type     VARCHAR(24)  NOT NULL DEFAULT 'NONE',
    early_fee_rate     DECIMAL(9,4)  NOT NULL DEFAULT 0,
    early_fee_amount   DECIMAL(18,2) NOT NULL DEFAULT 0,
    grace_days         INT NOT NULL DEFAULT 0,
    status             VARCHAR(24)  NOT NULL DEFAULT 'ACTIVE',
    note               TEXT,
    contract_url       VARCHAR(1000),
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_debts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_debts_creditor FOREIGN KEY (creditor_id) REFERENCES creditors (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------
-- repayment_schedules: lịch trả gốc + lãi theo từng kỳ
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS repayment_schedules (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    debt_id        BIGINT NOT NULL,
    installment_no INT NOT NULL,
    period_start   DATE NOT NULL,
    period_end     DATE NOT NULL,
    due_date       DATE NOT NULL,
    principal_due  DECIMAL(18,2) NOT NULL DEFAULT 0,
    interest_due   DECIMAL(18,2) NOT NULL DEFAULT 0,
    fee_due        DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_due      DECIMAL(18,2) NOT NULL DEFAULT 0,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0,
    interest_paid  DECIMAL(18,2) NOT NULL DEFAULT 0,
    fee_paid       DECIMAL(18,2) NOT NULL DEFAULT 0,
    paid_amount    DECIMAL(18,2) NOT NULL DEFAULT 0,
    status         VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    paid_at        DATE,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schedule_debt_no (debt_id, installment_no),
    CONSTRAINT fk_schedule_debt FOREIGN KEY (debt_id) REFERENCES debts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------
-- interest_rate_history: lịch sử điều chỉnh lãi suất
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interest_rate_history (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    debt_id        BIGINT NOT NULL,
    annual_rate    DECIMAL(9,4) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to   DATE NULL,
    note           VARCHAR(500),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rate_debt FOREIGN KEY (debt_id) REFERENCES debts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------
-- payments: các lần trả nợ
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    debt_id     BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    amount      DECIMAL(18,2) NOT NULL,
    surplus     DECIMAL(18,2) NOT NULL DEFAULT 0,
    paid_at     DATE NOT NULL,
    method      VARCHAR(32) NOT NULL DEFAULT 'CASH',
    note        VARCHAR(500),
    status      VARCHAR(16) NOT NULL DEFAULT 'RECORDED',
    reversal_of BIGINT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_debt FOREIGN KEY (debt_id) REFERENCES debts (id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------
-- payment_allocations: tiền trả được phân bổ vào gốc / lãi / phí
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_allocations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id  BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    component   VARCHAR(24) NOT NULL,
    amount      DECIMAL(18,2) NOT NULL,
    CONSTRAINT fk_alloc_payment FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE,
    CONSTRAINT fk_alloc_schedule FOREIGN KEY (schedule_id) REFERENCES repayment_schedules (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------
-- reminders: nhắc hạn tự động (sinh mỗi ngày từ 5 ngày trước hạn)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reminders (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    debt_id     BIGINT NOT NULL,
    schedule_id BIGINT NULL,
    remind_type VARCHAR(24) NOT NULL DEFAULT 'DAILY',
    notify_date DATE NULL,
    channel     VARCHAR(16) NOT NULL DEFAULT 'EMAIL',
    remind_at   DATE NOT NULL,
    priority    INT NOT NULL DEFAULT 0,
    status      VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    sent_at     DATETIME NULL,
    message     VARCHAR(1000),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Mỗi kỳ trả chỉ được nhắc tối đa 1 lần/ngày.
    UNIQUE KEY uk_reminder_notify_once (schedule_id, notify_date),
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_debt FOREIGN KEY (debt_id) REFERENCES debts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
