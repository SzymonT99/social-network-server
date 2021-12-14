package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.LikedPost;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LikedPostRepository extends JpaRepository<LikedPost, Long> {

    Optional<LikedPost> findByPostAndLikedPostUser(Post post, User user);

    boolean existsByPostAndLikedPostUser(Post post, User user);
}
