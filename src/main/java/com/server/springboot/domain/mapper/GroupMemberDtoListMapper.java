package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.AddressDto;
import com.server.springboot.domain.dto.response.GroupMemberDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Address;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupMemberDtoListMapper implements Converter<List<GroupMemberDto>, List<GroupMember>> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<AddressDto, Address> addressDtoMapper;

    @Autowired
    public GroupMemberDtoListMapper(Converter<UserDto, User> userDtoMapper, Converter<AddressDto, Address> addressDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.addressDtoMapper = addressDtoMapper;
    }

    @Override
    public List<GroupMemberDto> convert(List<GroupMember> from) {
        List<GroupMemberDto> groupMemberDtoList = new ArrayList<>();

        for (GroupMember groupMember : from) {
            GroupMemberDto groupMemberDto = GroupMemberDto.builder()
                    .groupMemberId(groupMember.getGroupMemberId())
                    .user(userDtoMapper.convert(groupMember.getMember()))
                    .address(groupMember.getMember().getUserProfile().getAddress() != null
                            ? addressDtoMapper.convert(groupMember.getMember().getUserProfile().getAddress()) : null)
                    .groupPermissionType(groupMember.getGroupPermissionType())
                    .groupMemberStatus(groupMember.getGroupMemberStatus())
                    .addedIn(groupMember.getAddedIn() != null
                            ? groupMember.getAddedIn().toString() : null)
                    .invitationDisplayed(groupMember.isInvitationDisplayed())
                    .build();

            groupMemberDtoList.add(groupMemberDto);
        }
        return groupMemberDtoList;
    }
}