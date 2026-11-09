package com.tronno.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tronno.app.models.InterestRateHistory;

/**
 * InterestRateHistoryRepository
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

public interface InterestRateHistoryRepository extends JpaRepository<InterestRateHistory, Long> {
    List<InterestRateHistory> findByDebtIdOrderByEffectiveFromAsc(Long debtId);
    List<InterestRateHistory> findByDebtIdAndEffectiveFromLessThanEqual(Long debtId, LocalDate date);
}
