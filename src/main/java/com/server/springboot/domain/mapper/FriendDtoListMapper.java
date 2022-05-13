package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.AddressDto;
import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Address;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FriendDtoListMapper implements Converter<List<FriendDto>, List<Friend>> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<UserDto>, List<User>> userDtoListMapper;
    private final Converter<AddressDto, Address> addressDtoMapper;

    @Autowired
    public FriendDtoListMapper() {
        this.userDtoMapper = new UserDtoMapper();
        this.userDtoListMapper = new UserDtoListMapper();
        this.addressDtoMapper = new AddressDtoMapper();
    }

    @Override
    public List<FriendDto> convert(List<Friend> from) {
        List<FriendDto> friendDtoList = new ArrayList<>();

        from = from.stream()
                .sorted(Comparator.comparing(Friend::getFriendFromDate).reversed())
                .collect(Collectors.toList());

        for (Friend friend : from) {

            List<User> friendsOfUserFriend = friend.getUserFriend().getFriends().stream()
                    .filter((userFriend) -> userFriend.getIsInvitationAccepted() != null && userFriend.getIsInvitationAccepted())
                    .map(Friend::getUserFriend)
                    .collect(Collectors.toList());

            FriendDto friendDto = FriendDto.builder()
                    .friendId(friend.getFriendId())
                    .isInvitationAccepted(friend.getIsInvitationAccepted() != null ? friend.getIsInvitationAccepted() : null)
                    .invitationDate(friend.getInvitationDate() != null ? friend.getInvitationDate().toString() : null)
                    .friendFromDate(friend.getFriendFromDate() != null
                            ? friend.getFriendFromDate().toString() : null)
                    .address(friend.getUserFriend().getUserProfile().getAddress() != null
                            ? addressDtoMapper.convert(friend.getUserFriend().getUserProfile().getAddress())
                            : null)
                    .user(userDtoMapper.convert(friend.getUserFriend()))
                    .userFriends(userDtoListMapper.convert(friendsOfUserFriend))
                    .build();

            friendDtoList.add(friendDto);
        }

        return friendDtoList;
    }
}
