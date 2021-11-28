package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.SharedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedPostRepository extends JpaRepository<SharedPost, Long> {
}
