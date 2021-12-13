package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.SharedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SharedPostRepository extends JpaRepository<SharedPost, Long> {

    @Transactional
    void deleteBySharedPostId(Long sharedPostId);

    boolean existsBySharedPostId(Long sharedPostId);
}
