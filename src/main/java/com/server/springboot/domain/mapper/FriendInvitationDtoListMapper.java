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

@Component
public class FriendInvitationDtoListMapper implements Converter<List<FriendInvitationDto>, List<Friend>> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public FriendInvitationDtoListMapper(Converter<UserDto, User> userDtoMapper) {
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<FriendInvitationDto> convert(List<Friend> from) {
        List<FriendInvitationDto> friendInvitationDtoList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        for (Friend friend : from) {
            FriendInvitationDto friendInvitationDto = FriendInvitationDto.builder()
                    .isInvitationAccepted(friend.getIsInvitationAccepted())
                    .invitationDisplayed(friend.isInvitationDisplayed())
                    .invitationDate(friend.getInvitationDate().format(formatter))
                    .invitingUser(userDtoMapper.convert(friend.getUser()))
                    .build();

            friendInvitationDtoList.add(friendInvitationDto);
        }

        return friendInvitationDtoList;
    }
}