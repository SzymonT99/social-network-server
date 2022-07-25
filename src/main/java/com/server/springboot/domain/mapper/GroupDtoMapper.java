package com.server.springboot.domain.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupDtoMapper implements Converter<GroupDto, Group> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;
    private final Converter<List<GroupMemberDto>, List<GroupMember>> groupMemberDtoListMapper;

    @Autowired
    public GroupDtoMapper(Converter<ImageDto, Image> imageDtoMapper,
                          Converter<UserDto, User> userDtoMapper,
                          Converter<List<InterestDto>, List<Interest>> interestDtoListMapper,
                          Converter<List<GroupMemberDto>, List<GroupMember>> groupMemberDtoListMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.interestDtoListMapper = interestDtoListMapper;
        this.groupMemberDtoListMapper = groupMemberDtoListMapper;
    }

    @Override
    public GroupDto convert(Group from) {

        List<GroupMember> filteredGroupMembers = from.getGroupMembers().stream()
                .filter((groupMember -> groupMember.getGroupMemberStatus() == GroupMemberStatus.JOINED))
                .collect(Collectors.toList());

        return GroupDto.builder()
                .groupId(from.getGroupId())
                .name(from.getName())
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .interests(interestDtoListMapper.convert(Lists.newArrayList(from.getGroupInterests())))
                .createdAt(from.getCreatedAt().toString())
                .members(groupMemberDtoListMapper.convert(filteredGroupMembers))
                .isPublic(from.isPublic())
                .groupCreator(userDtoMapper.convert(from.getGroupCreator()))
                .postsNumber(from.getPosts().stream().count())
                .build();
    }
}

