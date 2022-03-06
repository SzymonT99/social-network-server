package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.SharedPost;
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
public interface SharedPostRepository extends JpaRepository<SharedPost, Long> {

    @Transactional
    void deleteBySharedPostId(Long sharedPostId);

    boolean existsBySharedPostId(Long sharedPostId);

    List<SharedPost> findAllBySharedPostUserInAndDateIsGreaterThan(List<User> users, LocalDateTime dateLimit);

    List<SharedPost> findAllByBasePostIn(List<Post> posts);

    @Modifying
    @Query("UPDATE SharedPost s SET s.isPostAuthorNotified = :isPostAuthorNotified where s.basePost in :posts")
    void setPostAuthorNotificationDisplayed(@Param("isPostAuthorNotified") boolean isPostAuthorNotified,
                                            @Param("posts") List<Post> posts);
}
