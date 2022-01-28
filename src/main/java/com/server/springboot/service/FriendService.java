package com.server.springboot.service;

import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.FriendInvitationDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendService {

    void inviteToFriendsByUserId(Long userId);

    @Transactional
    List<FriendInvitationDto> findAllUserInvitationsToFriends(Long userId);

    void respondToFriendInvitation(Long inviterId, String reactionToInvitation);

    @Transactional
    void deleteFriendById(Long friendId);

    List<FriendDto> findAllUserFriends(Long userId);
}
