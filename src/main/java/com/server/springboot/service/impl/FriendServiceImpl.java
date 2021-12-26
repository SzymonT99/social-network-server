package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.FriendInvitationDto;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.FriendRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.*;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final JwtUtils jwtUtils;
    private final Converter<List<FriendInvitationDto>, List<Friend>> friendInvitationDtoListMapper;
    private final  Converter<List<FriendDto>, List<Friend>> friendDtoListMapper;

    @Autowired
    public FriendServiceImpl(UserRepository userRepository, FriendRepository friendRepository, JwtUtils jwtUtils,
                             Converter<List<FriendInvitationDto>, List<Friend>> friendInvitationDtoListMapper,
                             Converter<List<FriendDto>, List<Friend>> friendDtoListMapper) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.jwtUtils = jwtUtils;
        this.friendInvitationDtoListMapper = friendInvitationDtoListMapper;
        this.friendDtoListMapper = friendDtoListMapper;
    }

    @Override
    public void inviteToFriendsByUserId(Long userId) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        User invitedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        if (friendRepository.existsByUserAndUserFriend(user, invitedUser)) {
            throw new ConflictRequestException("Friend request has already been sent to user with id: " + userId);
        }
        Friend friend = Friend.builder()
                .user(user)
                .userFriend(invitedUser)
                .isInvitationAccepted(null)
                .invitationDate(LocalDateTime.now())
                .invitationDisplayed(false)
                .friendFromDate(null)
                .build();
        friendRepository.save(friend);
    }

    @Override
    public List<FriendInvitationDto> findAllUserInvitationsToFriends() {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User userFriend = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        List<Friend> friends = friendRepository.findByUserFriendAndIsInvitationAccepted(userFriend, null);
        friendRepository.setFriendInvitationDisplayed(true, userFriend);
        return friendInvitationDtoListMapper.convert(friends);
    }

    @Override
    public void respondToFriendInvitation(Long inviterId, String reactionToInvitation) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User currentUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new NotFoundException("Not found inviter to friends with id: " + inviterId));
        Friend friend = friendRepository.findByUserAndUserFriend(inviter, currentUser)
                .orElseThrow(() -> new NotFoundException("Not found friend record with user id: "
                        + inviter + " and user friend id: " + currentUser));
        if (friend.getIsInvitationAccepted() != null) {
            throw new ConflictRequestException("The user has already responded to the invitation");
        }

        if (reactionToInvitation.equals("accept")) {
            friend.setIsInvitationAccepted(true);
            friend.setFriendFromDate(LocalDate.now());
        } else if (reactionToInvitation.equals("reject")) {
            friend.setIsInvitationAccepted(false);
        } else {
            throw new BadRequestException("Unknown reaction to invitation to friends");
        }
        friendRepository.save(friend);
    }

    @Override
    public void deleteFriendById(Long friendId) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User currentUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Not found friend with id: " + friendId));
        if (friend.getUser() != currentUser) {
            throw new ForbiddenException("Invalid user inviter - friend deleting access forbidden");
        }

        friendRepository.delete(friend);
    }

    @Override
    public List<FriendDto> findAllUserFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<Friend> userFriends = friendRepository.findByUser(user);
        return friendDtoListMapper.convert(userFriends);
    }
}
