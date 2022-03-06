package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupInvitationDtoListMapper implements Converter<List<GroupInvitationDto>, List<GroupMember>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;

    @Autowired
    public GroupInvitationDtoListMapper(Converter<ImageDto, Image> imageDtoMapper,
                                        Converter<List<InterestDto>, List<Interest>> interestDtoListMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.interestDtoListMapper = interestDtoListMapper;
    }

    @Override
    public List<GroupInvitationDto> convert(List<GroupMember> from) {
        List<GroupInvitationDto> groupInvitationDtoList = new ArrayList<>();

        for (GroupMember groupMember : from) {
            GroupInvitationDto groupInvitationDto = GroupInvitationDto.builder()
                    .invitationDisplayed(groupMember.isInvitationDisplayed())
                    .invitationDate(groupMember.getInvitationDate().toString())
                    .groupId(groupMember.getGroup().getGroupId())
                    .groupName(groupMember.getGroup().getName())
                    .groupDescription(groupMember.getGroup().getDescription())
                    .groupImage(groupMember.getGroup().getImage() != null
                            ? imageDtoMapper.convert(groupMember.getGroup().getImage()) : null)
                    .groupMembersNumber(groupMember.getGroup().getGroupMembers().size())
                    .groupCreatedAt(groupMember.getGroup().getCreatedAt().toString())
                    .groupInterests(interestDtoListMapper.convert(Lists.newArrayList(groupMember.getGroup().getGroupInterests())))
                    .build();

            groupInvitationDtoList.add(groupInvitationDto);
        }

        return groupInvitationDtoList;
    }
}
