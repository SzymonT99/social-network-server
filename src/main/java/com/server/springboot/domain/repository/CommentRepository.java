package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Transactional
    void deleteByCommentId(Long commentId);

    List<Comment> findAllByCommentAuthor(User user);

    List<Comment> findAllByCommentAuthorInAndCreatedAtIsGreaterThan(List<User> users, LocalDateTime dateLimit);

    List<Comment> findAllByCommentedPostIn(List<Post> posts);

    @Modifying
    @Query("UPDATE Comment c SET c.isPostAuthorNotified = :isPostAuthorNotified where c.commentedPost in :posts")
    void setPostAuthorNotificationDisplayed(@Param("isPostAuthorNotified") boolean isPostAuthorNotified,
                                            @Param("posts") List<Post> posts);
}
