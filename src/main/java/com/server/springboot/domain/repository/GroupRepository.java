package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.Interest;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Transactional
    void deleteByGroupId(Long groupId);

    List<Group> findByIsDeletedAndIsPublicOrderByCreatedAtDesc(boolean isDeleted, boolean isPublic);

    List<Group> findByGroupCreator(User user);

    List<Group> findByGroupInterestsInAndIsDeletedAndIsPublicOrderByCreatedAtDesc(List<Interest> interests, boolean isDeleted, boolean isPublic);
}
