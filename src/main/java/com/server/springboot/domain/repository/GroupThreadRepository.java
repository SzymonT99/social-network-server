package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.GroupThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GroupThreadRepository extends JpaRepository<GroupThread, Long> {

    @Transactional
    void deleteByThreadId(Long threadId);
}
