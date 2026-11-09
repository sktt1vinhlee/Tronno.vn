package com.tronno.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

import lombok.*;

/**
 * PaymentAllocation
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
@Table(name = "payment_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(nullable = false, length = 24)
    private String component;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
}
