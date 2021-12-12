package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByPostId(Long id);

    void deleteByPostId(Long id);

    List<Post> findByIsDeletedOrderByCreatedAtDesc(boolean isDeleted);

}
