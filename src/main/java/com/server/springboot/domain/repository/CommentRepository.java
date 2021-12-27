package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Transactional
    void deleteByCommentId(Long commentId);

    List<Comment> findAllByCommentAuthorInAndCreatedAtIsGreaterThan(List<User> users, LocalDateTime dateLimit);
}
