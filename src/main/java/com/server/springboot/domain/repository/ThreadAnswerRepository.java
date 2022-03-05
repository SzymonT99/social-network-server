package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.ThreadAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ThreadAnswerRepository extends JpaRepository<ThreadAnswer, Long> {

    @Transactional
    void deleteByAnswerId(Long answerId);
}
