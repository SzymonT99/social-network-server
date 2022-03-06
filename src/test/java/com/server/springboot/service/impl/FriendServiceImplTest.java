package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.FriendInvitationDto;
import com.server.springboot.domain.repository.FriendRepository;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FriendService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class FriendServiceImplTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("Invite to friends user with id: 12")
    @Order(value = 1)
    void inviteToFriendsByUserId() {
        friendService.inviteToFriendsByUserId(12L);

        assertEquals(12L, (long) friendRepository.findById(12L).get().getUserFriend().getUserId());
    }

    @Test
    @DisplayName("Get invitations to friend")
    @Order(value = 2)
    void findAllUserInvitationsToFriends() {
        Long userId  = jwtUtils.getLoggedUserId();

        assertThat(friendService.findAllUserReceivedInvitationsToFriends(userId, true))
                .extracting(FriendInvitationDto::getFriendId,FriendInvitationDto::getIsInvitationAccepted)
                .containsExactly(tuple(62L, false));

    }

    @Test
    @DisplayName("Get friends")
    @Order(value = 3)
    void findAllUserFriends() {
        Long userId  = jwtUtils.getLoggedUserId();

        assertThat(friendService.findAllUserFriends(userId))
                .extracting(FriendDto::getFriendId,FriendDto::getIsInvitationAccepted)
                .containsExactly(tuple(62L, false));
    }

    @Test
    @DisplayName("Respond to friend invitation from user with id: 12")
    @Order(value = 3)
    void respondToFriendInvitation() {

        friendService.respondToFriendInvitation(12L, "accept");

        assertTrue(friendRepository.findById(62L).get().getIsInvitationAccepted());
    }

    @Test
    @DisplayName("Delete friend by id: 62")
    @Order(value = 4)
    void deleteFriendById() {

        friendService.deleteFriendById(62L, false);

        assertThatThrownBy(() -> friendRepository.findById(62L))
                .isInstanceOf(BadRequestException.class);
    }

}
