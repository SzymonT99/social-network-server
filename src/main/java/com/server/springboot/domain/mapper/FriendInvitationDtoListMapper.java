package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.FriendInvitationDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FriendInvitationDtoListMapper implements Converter<List<FriendInvitationDto>, List<Friend>> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<UserDto>, List<User>> userDtoListMapper;

    @Autowired
    public FriendInvitationDtoListMapper() {
        this.userDtoMapper = new UserDtoMapper();
        this.userDtoListMapper = new UserDtoListMapper();
    }

    @Override
    public List<FriendInvitationDto> convert(List<Friend> from) {
        List<FriendInvitationDto> friendInvitationDtoList = new ArrayList<>();

        for (Friend friend : from) {

            List<User> currentFriends = friend.getUser().getFriends().stream()
                    .filter(el -> el.getIsInvitationAccepted() != null && el.getIsInvitationAccepted())
                    .map(Friend::getUserFriend)
                    .collect(Collectors.toList());

            FriendInvitationDto friendInvitationDto = FriendInvitationDto.builder()
                    .friendId(friend.getFriendId())
                    .isInvitationAccepted(friend.getIsInvitationAccepted())
                    .invitationDisplayed(friend.isInvitationDisplayed())
                    .invitationDate(friend.getInvitationDate().toString())
                    .invitingUser(userDtoMapper.convert(friend.getUser()))
                    .invitingUserFriends(userDtoListMapper.convert(currentFriends))
                    .build();

            friendInvitationDtoList.add(friendInvitationDto);
        }

        return friendInvitationDtoList;
    }
}