package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupMemberDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberDtoMapper implements Converter<GroupMemberDto, GroupMember> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public GroupMemberDtoMapper(Converter<UserDto, User> userDtoMapper) {
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public GroupMemberDto convert(GroupMember from) {
        return GroupMemberDto.builder()
                .groupMemberId(from.getGroupMemberId())
                .member(userDtoMapper.convert(from.getGroupMember()))
                .groupPermissionType(from.getGroupPermissionType())
                .groupMemberStatus(from.getGroupMemberStatus())
                .addedIn(from.getAddedIn().toString())
                .invitationDisplayed(from.isInvitationDisplayed())
                .build();
    }
}