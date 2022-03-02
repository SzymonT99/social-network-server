package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Transactional
    void deleteByGroupId(Long groupId);

    List<Group> findByIsDeletedAndIsPublicOrderByCreatedAtDesc(boolean isDeleted, boolean isPublic);
}
