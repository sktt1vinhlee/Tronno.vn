package com.tronno.app.repository;

import com.tronno.app.models.InterestRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface InterestRateHistoryRepository extends JpaRepository<InterestRateHistory, Long> {
    List<InterestRateHistory> findByDebtIdOrderByEffectiveFromAsc(Long debtId);
    List<InterestRateHistory> findByDebtIdAndEffectiveFromLessThanEqual(Long debtId, LocalDate date);
}
