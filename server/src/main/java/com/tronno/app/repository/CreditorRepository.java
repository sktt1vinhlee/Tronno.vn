package com.tronno.app.repository;

import com.tronno.app.models.Creditor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CreditorRepository extends JpaRepository<Creditor, Long> {
    List<Creditor> findByUserIdOrderByNameAsc(Long userId);
    Optional<Creditor> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
