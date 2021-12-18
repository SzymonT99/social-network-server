package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.UserDetailsDto;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class FriendDtoListMapper implements Converter<List<FriendDto>, List<Friend>> {

    private final Converter<UserDetailsDto, User> userDetailsDtoMapper;

    @Autowired
    public FriendDtoListMapper(Converter<UserDetailsDto, User> userDetailsDtoMapper) {
        this.userDetailsDtoMapper = userDetailsDtoMapper;
    }

    @Override
    public List<FriendDto> convert(List<Friend> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        List<FriendDto> friendDtoList = new ArrayList<>();

        for (Friend friend : from) {
            FriendDto friendDto = FriendDto.builder()
                    .friendId(friend.getFriendId())
                    .isInvitationAccepted(friend.isInvitationAccepted())
                    .invitationDisplayed(friend.isInvitationDisplayed())
                    .invitationDate(friend.getInvitationDate().format(formatter))
                    .friendFromDate(friend.getFriendFromDate().format(formatter))
                    .userDetails(userDetailsDtoMapper.convert(friend.getUserFriend()))
                    .build();

            friendDtoList.add(friendDto);
        }

        return friendDtoList;
    }
}
