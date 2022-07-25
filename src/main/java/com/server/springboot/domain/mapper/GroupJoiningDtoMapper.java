package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.dto.response.GroupJoiningDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupJoiningDtoMapper implements Converter<GroupJoiningDto, GroupMember> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<GroupDto, Group> groupDtoMapper;

    @Autowired
    public GroupJoiningDtoMapper(Converter<UserDto, User> userDtoMapper,
                                 Converter<GroupDto, Group> groupDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.groupDtoMapper = groupDtoMapper;
    }

    @Override
    public GroupJoiningDto convert(GroupMember from) {
        return GroupJoiningDto.builder()
                .userMember(userDtoMapper.convert(from.getMember()))
                .groupMemberStatus(from.getGroupMemberStatus())
                .addedIn(from.getAddedIn().toString())
                .group(groupDtoMapper.convert(from.getGroup()))
                .build();
    }
}