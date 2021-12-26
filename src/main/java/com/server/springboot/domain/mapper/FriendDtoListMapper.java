package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class FriendDtoListMapper implements Converter<List<FriendDto>, List<Friend>> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public FriendDtoListMapper(Converter<UserDto, User> userDtoMapper) {
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<FriendDto> convert(List<Friend> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List<FriendDto> friendDtoList = new ArrayList<>();

        for (Friend friend : from) {
            FriendDto friendDto = FriendDto.builder()
                    .friendId(friend.getFriendId())
                    .isInvitationAccepted(friend.getIsInvitationAccepted())
                    .invitationDate(friend.getInvitationDate().format(formatter))
                    .friendFromDate(friend.getFriendFromDate() != null
                            ? friend.getFriendFromDate().format(formatter) : null)
                    .user(userDtoMapper.convert(friend.getUser()))
                    .build();

            friendDtoList.add(friendDto);
        }

        return friendDtoList;
    }
}
