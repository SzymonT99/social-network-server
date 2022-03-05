package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.ThreadAnswer;
import com.server.springboot.domain.entity.ThreadAnswerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ThreadAnswerReviewRepository extends JpaRepository<ThreadAnswerReview, Long> {

    @Transactional
    void deleteByAnswerReviewId(Long reviewId);

    boolean existsByThreadAnswerAndAnswerReviewAuthor(ThreadAnswer threadAnswer, GroupMember groupMember);
}

