package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupAndMember(Group group, User user);

    boolean existsByMemberAndGroup(User user, Group group);

    List<GroupMember> findByMemberAndGroupMemberStatus(User user, GroupMemberStatus groupMemberStatus);

    @Modifying
    @Query("UPDATE GroupMember gm SET gm.invitationDisplayed = :invitationDisplayed where gm.member = :member")
    void setGroupInvitationDisplayed(@Param("invitationDisplayed") boolean invitationDisplayed,
                                     @Param("member") User member);

    List<GroupMember> findByGroupAndGroupMemberStatus(Group group, GroupMemberStatus groupMemberStatus);
}
