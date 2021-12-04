package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.AccountVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountVerificationRepository extends JpaRepository<AccountVerification, Long> {

    Optional<AccountVerification> findByVerificationCode(String code);

}
