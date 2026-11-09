package com.tronno.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tronno.app.models.Creditor;

/**
 * CreditorRepository
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
public interface CreditorRepository extends JpaRepository<Creditor, Long> {
    List<Creditor> findByUserIdOrderByNameAsc(Long userId);
    Optional<Creditor> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
