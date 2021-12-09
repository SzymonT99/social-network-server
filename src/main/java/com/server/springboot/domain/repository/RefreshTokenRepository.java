package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.RefreshToken;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    boolean existsByUser(User user);

    @Transactional
    void deleteByUser(User user);

}