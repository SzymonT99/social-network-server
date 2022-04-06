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
public class GroupInvitationDtoListMapper implements Converter<List<GroupInvitationDto>, List<GroupMember>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<GroupMemberDto>, List<GroupMember>> groupMemberDtoListMapper;

    @Autowired
    public GroupInvitationDtoListMapper() {
        this.imageDtoMapper = new ImageDtoMapper();
        this.interestDtoListMapper = new InterestDtoListMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.groupMemberDtoListMapper = new GroupMemberDtoListMapper();
    }

    @Override
    public List<GroupInvitationDto> convert(List<GroupMember> from) {
        List<GroupInvitationDto> groupInvitationDtoList = new ArrayList<>();

        for (GroupMember groupMember : from) {

            List<GroupMember> filteredGroupMembers = groupMember.getGroup().getGroupMembers().stream()
                    .filter((member -> member.getGroupMemberStatus() == GroupMemberStatus.JOINED))
                    .collect(Collectors.toList());

            GroupInvitationDto groupInvitationDto = GroupInvitationDto.builder()
                    .invitationDisplayed(groupMember.isInvitationDisplayed())
                    .invitationDate(groupMember.getInvitationDate().toString())
                    .groupId(groupMember.getGroup().getGroupId())
                    .groupName(groupMember.getGroup().getName())
                    .groupCreator(userDtoMapper.convert(groupMember.getGroup().getGroupCreator()))
                    .groupImage(groupMember.getGroup().getImage() != null
                            ? imageDtoMapper.convert(groupMember.getGroup().getImage()) : null)
                    .groupMembers(groupMemberDtoListMapper.convert(filteredGroupMembers))
                    .groupPostsNumber(groupMember.getGroup().getPosts().size())
                    .groupCreatedAt(groupMember.getGroup().getCreatedAt().toString())
                    .groupInterests(interestDtoListMapper.convert(Lists.newArrayList(groupMember.getGroup().getGroupInterests())))
                    .build();

            groupInvitationDtoList.add(groupInvitationDto);
        }

        return groupInvitationDtoList;
    }
}
