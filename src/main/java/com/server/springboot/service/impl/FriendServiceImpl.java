package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.FriendRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.*;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FriendService;
import com.server.springboot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final JwtUtils jwtUtils;
    private final FriendInvitationDtoListMapper friendInvitationDtoListMapper;
    private final SentFriendInvitationDtoListMapper sentFriendInvitationDtoListMapper;
    private final FriendDtoListMapper friendDtoListMapper;
    private final ProfilePhotoDtoMapper profilePhotoDtoMapper;
    private final AddressDtoMapper addressDtoMapper;
    private final UserDtoListMapper userDtoListMapper;
    private final NotificationService notificationService;

    @Autowired
    public FriendServiceImpl(UserRepository userRepository, FriendRepository friendRepository, JwtUtils jwtUtils,

                             FriendInvitationDtoListMapper friendInvitationDtoListMapper,
                             SentFriendInvitationDtoListMapper sentFriendInvitationDtoListMapper,
                             FriendDtoListMapper friendDtoListMapper, ProfilePhotoDtoMapper profilePhotoDtoMapper,
                             AddressDtoMapper addressDtoMapper, UserDtoListMapper userDtoListMapper,
                             NotificationService notificationService) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.jwtUtils = jwtUtils;
        this.friendInvitationDtoListMapper = friendInvitationDtoListMapper;
        this.sentFriendInvitationDtoListMapper = sentFriendInvitationDtoListMapper;
        this.friendDtoListMapper = friendDtoListMapper;
        this.profilePhotoDtoMapper = profilePhotoDtoMapper;
        this.addressDtoMapper = addressDtoMapper;
        this.userDtoListMapper = userDtoListMapper;
        this.notificationService = notificationService;
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

        notificationService.sendNotificationToUser(user, invitedUser.getUserId(), ActionType.FRIEND_INVITATION);
    }

    @Override
    public List<FriendInvitationDto> findAllUserReceivedInvitationsToFriends(Long userId, boolean isDisplayed) {
        User userFriend = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        if (isDisplayed) {
            friendRepository.setFriendInvitationDisplayed(true, userFriend);
        }
        List<Friend> friends = friendRepository.findByUserFriendAndIsInvitationAccepted(userFriend, null);
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
            friend.setFriendFromDate(LocalDateTime.now());

            Friend acceptedFriend = Friend.builder()
                    .isInvitationAccepted(true)
                    .invitationDisplayed(true)
                    .invitationDate(LocalDateTime.now())
                    .friendFromDate(LocalDateTime.now())
                    .user(currentUser)
                    .userFriend(inviter)
                    .isUserNotifiedAboutAccepting(false)
                    .build();
            friendRepository.save(acceptedFriend);

            notificationService.sendNotificationToUser(currentUser, inviterId, ActionType.ACTIVITY_BOARD);

        } else if (reactionToInvitation.equals("reject")) {
            friend.setIsInvitationAccepted(false);
        } else {
            throw new BadRequestException("Unknown reaction to invitation to friends");
        }
        friendRepository.save(friend);
    }

    @Override
    @Transactional
    public void deleteFriendById(Long friendId, boolean isDeletedInvitation) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User currentUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Not found friend with id: " + friendId));

        User userFriend;
        if (isDeletedInvitation) {
            userFriend = friend.getUserFriend();
        } else {
            userFriend = friend.getUser();
        }
        if (friend.getUser() != currentUser && friend.getUserFriend() != currentUser) {
            throw new ForbiddenException("Invalid user inviter - friend deleting access forbidden");
        }

        friendRepository.deleteByUserAndUserFriend(currentUser, userFriend);
        friendRepository.deleteByUserAndUserFriend(userFriend, currentUser);
    }

    @Override
    public List<FriendDto> findAllUserFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<Friend> userFriends = friendRepository.findByUserAndIsInvitationAccepted(user, true);
        return friendDtoListMapper.convert(userFriends);
    }

    @Override
    public List<SentFriendInvitationDto> findAllUserSentInvitationsToFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        List<Friend> friends = friendRepository.findByUserAndIsInvitationAccepted(user, null);
        return sentFriendInvitationDtoListMapper.convert(friends);
    }

    @Override
    public List<FriendSuggestionDto> findAllFriendsSuggestions() {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        List<User> loggedUserFriends = loggedUser.getFriends().stream()
                .filter((friend) -> friend.getIsInvitationAccepted() != null && friend.getIsInvitationAccepted())
                .map(Friend::getUserFriend)
                .collect(Collectors.toList());

        List<User> users = userRepository.findAll();
        users = users.stream()
                .filter((userEl) -> !userEl.getUserId().equals(loggedUserId)
                        && !loggedUserFriends.contains(userEl)
                        && !friendRepository.existsByUserAndUserFriend(loggedUser, userEl)
                        && !friendRepository.existsByUserAndUserFriend(userEl, loggedUser))
                .collect(Collectors.toList());
        List<FriendSuggestionDto> friendSuggestionList = new ArrayList<>();

        for (User currentUser : users) {
            List<User> currentUserFriends = currentUser.getFriends().stream()
                    .filter((friendEl) -> friendEl.getIsInvitationAccepted() != null && friendEl.getIsInvitationAccepted())
                    .map(Friend::getUserFriend)
                    .collect(Collectors.toList());

            List<User> mutualFriends = new ArrayList<>();

            currentUserFriends.forEach((currentUserFriend) -> {
                if (loggedUserFriends.contains(currentUserFriend)) {
                    mutualFriends.add(currentUserFriend);
                }
            });

            if (mutualFriends.size() > 0) {
                FriendSuggestionDto friendSuggestionDto = FriendSuggestionDto.builder()
                        .userId(currentUser.getUserId())
                        .firstName(currentUser.getUserProfile().getFirstName())
                        .lastName(currentUser.getUserProfile().getLastName())
                        .profilePhoto(currentUser.getUserProfile().getProfilePhoto() != null
                                ? profilePhotoDtoMapper.convert(currentUser.getUserProfile().getProfilePhoto()) : null)
                        .address(currentUser.getUserProfile().getAddress() != null
                                ? addressDtoMapper.convert(currentUser.getUserProfile().getAddress()) : null)
                        .userFriends(userDtoListMapper.convert(currentUserFriends))
                        .mutualFriends(userDtoListMapper.convert((mutualFriends)))
                        .build();

                friendSuggestionList.add(friendSuggestionDto);
            }
        }

        return friendSuggestionList;
    }
}
