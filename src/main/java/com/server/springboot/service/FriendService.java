package com.server.springboot.service;

import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.FriendInvitationDto;
import com.server.springboot.domain.dto.response.FriendSuggestionDto;
import com.server.springboot.domain.dto.response.SentFriendInvitationDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendService {

    void inviteToFriendsByUserId(Long userId);

    @Transactional
    List<FriendInvitationDto> findAllUserReceivedInvitationsToFriends(Long userId, boolean isDisplayed);

    void respondToFriendInvitation(Long inviterId, String reactionToInvitation);

    @Transactional
    void deleteFriendById(Long friendId, boolean isDeletedInvitation);

    List<FriendDto> findAllUserFriends(Long userId);

    @Transactional
    List<SentFriendInvitationDto> findAllUserSentInvitationsToFriends(Long userId);

    List<FriendSuggestionDto> findAllFriendsSuggestions();
}
