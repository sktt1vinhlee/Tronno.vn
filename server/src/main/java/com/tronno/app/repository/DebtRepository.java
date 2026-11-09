package com.tronno.app.repository;

import com.tronno.app.models.Debt;
import com.tronno.app.models.DebtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Debt> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, DebtStatus status);
    Optional<Debt> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, DebtStatus status);

    @Query("select d from Debt d where d.userId = :userId and d.status = :status")
    List<Debt> findActiveForUser(Long userId, DebtStatus status);
}
