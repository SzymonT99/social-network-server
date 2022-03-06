package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.SentFriendInvitationDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SentFriendInvitationDtoListMapper implements Converter<List<SentFriendInvitationDto>, List<Friend>> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<UserDto>, List<User>> userDtoListMapper;

    @Autowired
    public SentFriendInvitationDtoListMapper(Converter<UserDto, User> userDtoMapper, Converter<List<UserDto>, List<User>> userDtoListMapper) {
        this.userDtoMapper = userDtoMapper;
        this.userDtoListMapper = userDtoListMapper;
    }

    @Override
    public List<SentFriendInvitationDto> convert(List<Friend> from) {
        List<SentFriendInvitationDto> friendInvitationDtoList = new ArrayList<>();

        for (Friend friend : from) {

            List<User> currentFriends = friend.getUser().getFriends().stream()
                    .filter(el -> el.getIsInvitationAccepted() != null && el.getIsInvitationAccepted())
                    .map(Friend::getUserFriend)
                    .collect(Collectors.toList());

            SentFriendInvitationDto sentFriendInvitationDto = SentFriendInvitationDto.builder()
                    .friendId(friend.getFriendId())
                    .isInvitationAccepted(friend.getIsInvitationAccepted())
                    .invitationDisplayed(friend.isInvitationDisplayed())
                    .invitationDate(friend.getInvitationDate().toString())
                    .invitedUser(userDtoMapper.convert(friend.getUserFriend()))
                    .invitedUserFriends(userDtoListMapper.convert(currentFriends))
                    .build();

            friendInvitationDtoList.add(sentFriendInvitationDto);
        }

        return friendInvitationDtoList;
    }
}
