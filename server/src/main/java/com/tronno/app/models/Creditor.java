package com.tronno.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

/**
 * Creditor
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
@Table(name = "creditors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Creditor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private CreditorKind kind = CreditorKind.PERSON;

    @Column(length = 30)
    private String phone;

    @Column(length = 190)
    private String email;

    @Column(length = 1000)
    private String note;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Tạo chủ nợ với các thông tin cơ bản.
     *
     * @param userId id chủ sở hữu
     * @param name tên chủ nợ
     * @param kind loại chủ nợ
     */
    public Creditor(Long userId, String name, CreditorKind kind) {
        this.userId = userId;
        this.name = name;
        this.kind = kind;
        this.createdAt = LocalDateTime.now();
    }
}
