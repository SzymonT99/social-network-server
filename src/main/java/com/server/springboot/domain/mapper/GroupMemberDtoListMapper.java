package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupMemberDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupMemberDtoListMapper implements Converter<List<GroupMemberDto>, List<GroupMember>> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public GroupMemberDtoListMapper(Converter<UserDto, User> userDtoMapper) {
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<GroupMemberDto> convert(List<GroupMember> from) {
        List<GroupMemberDto> groupMemberDtoList = new ArrayList<>();

        for (GroupMember groupMember : from) {
            GroupMemberDto groupMemberDto = GroupMemberDto.builder()
                    .groupMemberId(groupMember.getGroupMemberId())
                    .member(userDtoMapper.convert(groupMember.getGroupMember()))
                    .groupPermissionType(groupMember.getGroupPermissionType())
                    .groupMemberStatus(groupMember.getGroupMemberStatus())
                    .addedIn(groupMember.getAddedIn().toString())
                    .invitationDisplayed(groupMember.isInvitationDisplayed())
                    .build();

            groupMemberDtoList.add(groupMemberDto);
        }
        return groupMemberDtoList;
    }
}