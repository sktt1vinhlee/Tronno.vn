package com.tronno.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;

/**
 * DebtRepository
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

public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Debt> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, DebtStatus status);
    Optional<Debt> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, DebtStatus status);

    @Query("select d from Debt d where d.userId = :userId and d.status = :status")
    List<Debt> findActiveForUser(Long userId, DebtStatus status);
}
