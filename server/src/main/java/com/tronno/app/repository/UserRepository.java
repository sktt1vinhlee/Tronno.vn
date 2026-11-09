package com.tronno.app.repository;

import com.tronno.app.models.User;
import com.tronno.app.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleOrderByFullNameAsc(UserRole role);

    List<User> findByRoleAndBlockedFalseOrderByFullNameAsc(UserRole role);
}
