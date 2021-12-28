package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUserFriendAndIsInvitationAccepted(User userFriend, Boolean isInvitationAccepted);

    boolean existsByUserAndUserFriend(User user, User userFriend);

    List<Friend> findByUser(User user);

    Optional<Friend> findByUserAndUserFriend(User user, User userFriend);

    @Modifying
    @Query("UPDATE Friend f SET f.invitationDisplayed = :invitationDisplayed where f.userFriend = :userFriend")
    void setFriendInvitationDisplayed(@Param("invitationDisplayed") boolean invitationDisplayed,
                                      @Param("userFriend") User userFriend);

    List<Friend> findByUserAndIsInvitationAccepted(User user, boolean IsInvitationAccepted);

    @Modifying
    @Query("UPDATE Friend f SET f.isUserNotifiedAboutAccepting = :isUserNotifiedAboutAccepting where f.user = :user")
    void setUserNotificationDisplayed(@Param("isUserNotifiedAboutAccepting") boolean isUserNotifiedAboutAccepting,
                                      @Param("user") User user);
}