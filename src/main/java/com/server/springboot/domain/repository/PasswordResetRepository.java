package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.PasswordReset;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByUserAndResetCode(User user, String resetToken);
}
