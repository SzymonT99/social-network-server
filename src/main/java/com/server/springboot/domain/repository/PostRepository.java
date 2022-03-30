package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.SharedPost;
import com.server.springboot.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByPostId(Long id);

    @Transactional
    void deleteByPostId(Long id);

    Page<Post> findByIsDeletedAndIsPublicOrderByCreatedAtDesc(boolean isDeleted, boolean isPublic, Pageable pageable);

    List<Post> findByFavourites(User user);

    List<Post> findByPostAuthor(User user);

    List<Post> findAllByPostAuthorInAndCreatedAtIsGreaterThanAndIsDeleted(List<User> users, LocalDateTime dateLimit, boolean isDeleted);

    List<Post> findAllByGroupInAndCreatedAtIsGreaterThanAndIsDeleted(List<Group> groups, LocalDateTime dateLimit, boolean isDeleted);

    List<Post> findAllByGroupIn(List<Group> groups);
}
