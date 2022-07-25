package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {

    @Transactional
    void deleteByImageId(String id);

    List<Image> findAllByImageIdInAndAddedInIsGreaterThan(List<String> imageIdList, LocalDateTime dateLimit);

}
