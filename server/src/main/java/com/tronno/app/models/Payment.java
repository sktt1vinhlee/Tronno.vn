package com.tronno.app.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "debt_id", nullable = false)
    private Long debtId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal surplus = BigDecimal.ZERO;

    @Column(name = "paid_at", nullable = false)
    private LocalDate paidAt;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String method = "CASH";

    @Column(length = 500)
    private String note;

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String status = "RECORDED";

    @Column(name = "reversal_of")
    private Long reversalOf;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
