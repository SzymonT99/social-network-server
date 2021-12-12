package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {

    @Transactional
    void deleteByImageId(String id);

}
