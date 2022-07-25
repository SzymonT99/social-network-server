package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupDtoListMapper implements Converter<List<GroupDto>, List<Group>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;
    private final Converter<List<GroupMemberDto>, List<GroupMember>> groupMemberDtoListMapper;

    @Autowired
    public GroupDtoListMapper() {
        this.imageDtoMapper = new ImageDtoMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.interestDtoListMapper = new InterestDtoListMapper();
        this.groupMemberDtoListMapper = new GroupMemberDtoListMapper();
    }

    @Override
    public List<GroupDto> convert(List<Group> from) {
        List<GroupDto> groupDtoList = new ArrayList<>();
        for (Group group : from) {

            List<GroupMember> filteredGroupMembers = group.getGroupMembers().stream()
                    .filter((groupMember -> groupMember.getGroupMemberStatus() == GroupMemberStatus.JOINED))
                    .collect(Collectors.toList());

            GroupDto groupDto = GroupDto.builder()
                    .groupId(group.getGroupId())
                    .name(group.getName())
                    .image(group.getImage() != null ? imageDtoMapper.convert(group.getImage()) : null)
                    .interests(interestDtoListMapper.convert(Lists.newArrayList(group.getGroupInterests())))
                    .createdAt(group.getCreatedAt().toString())
                    .isPublic(group.isPublic())
                    .groupCreator(userDtoMapper.convert(group.getGroupCreator()))
                    .members(groupMemberDtoListMapper.convert(filteredGroupMembers))
                    .postsNumber(group.getPosts().stream().count())
                    .build();

            groupDtoList.add(groupDto);
        }
        return groupDtoList;
    }
}
