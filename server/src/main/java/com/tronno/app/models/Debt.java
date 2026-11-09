package com.tronno.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

/**
 * Debt
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
@Entity
@Table(name = "debts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "creditor_id")
    private Long creditorId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "debt_type", nullable = false, length = 32)
    private DebtType debtType;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal principal;

    @Column(name = "annual_rate", nullable = false, precision = 9, scale = 4)
    @Builder.Default
    private BigDecimal annualRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_method", nullable = false, length = 24)
    @Builder.Default
    private InterestMethod interestMethod = InterestMethod.REDUCING;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 24)
    @Builder.Default
    private ScheduleType scheduleType = ScheduleType.EQUAL_PRINCIPAL;

    @Column(name = "disbursement_date", nullable = false)
    private LocalDate disbursementDate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    /**
     * Kiểu thời hạn: FIXED_DATE (chọn ngày đáo hạn) hoặc QUARTERLY (trả theo quý).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 24)
    @Builder.Default
    private TermType termType = TermType.FIXED_DATE;

    /**
     * Ngày đáo hạn cụ thể (dùng khi termType = FIXED_DATE).
     */
    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "installment_amount", precision = 18, scale = 2)
    private BigDecimal installmentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "late_fee_type", nullable = false, length = 24)
    @Builder.Default
    private LateFeeType lateFeeType = LateFeeType.NONE;

    @Column(name = "late_fee_rate", nullable = false, precision = 12, scale = 8)
    @Builder.Default
    private BigDecimal lateFeeRate = BigDecimal.ZERO;

    @Column(name = "late_fee_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal lateFeeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "early_fee_type", nullable = false, length = 24)
    @Builder.Default
    private EarlyFeeType earlyFeeType = EarlyFeeType.NONE;

    @Column(name = "early_fee_rate", nullable = false, precision = 9, scale = 4)
    @Builder.Default
    private BigDecimal earlyFeeRate = BigDecimal.ZERO;

    @Column(name = "early_fee_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal earlyFeeAmount = BigDecimal.ZERO;

    @Column(name = "grace_days", nullable = false)
    @Builder.Default
    private Integer graceDays = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private DebtStatus status = DebtStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "contract_url", length = 1000)
    private String contractUrl;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
