package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Transactional
    void deleteByCommentId(Long commentId);
}
