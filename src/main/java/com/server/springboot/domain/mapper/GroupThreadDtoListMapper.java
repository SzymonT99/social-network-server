package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.Thread;
import com.server.springboot.domain.entity.ThreadAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupThreadDtoListMapper implements Converter<List<GroupThreadDto>, List<Thread>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<GroupMemberDto, GroupMember> groupMemberDtoMapper;
    private final Converter<List<GroupThreadAnswerDto>, List<ThreadAnswer>> groupThreadAnswerDtoListMapper;

    @Autowired
    public GroupThreadDtoListMapper(Converter<ImageDto, Image> imageDtoMapper,
                                    Converter<GroupMemberDto, GroupMember> groupMemberDtoMapper,
                                    Converter<List<GroupThreadAnswerDto>, List<ThreadAnswer>> groupThreadAnswerDtoListMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.groupMemberDtoMapper = groupMemberDtoMapper;
        this.groupThreadAnswerDtoListMapper = groupThreadAnswerDtoListMapper;
    }

    @Override
    public List<GroupThreadDto> convert(List<Thread> from) {
        List<GroupThreadDto> groupThreadDtoList = new ArrayList<>();

        for (Thread thread : from) {
            GroupThreadDto groupThreadDto = GroupThreadDto.builder()
                    .threadId(thread.getThreadId())
                    .title(thread.getTitle())
                    .content(thread.getContent())
                    .image(thread.getImage() != null ? imageDtoMapper.convert(thread.getImage()) : null)
                    .createdAt(thread.getCreatedAt().toString())
                    .author(groupMemberDtoMapper.convert(thread.getThreadAuthor()))
                    .answers(groupThreadAnswerDtoListMapper.convert(Lists.newArrayList(thread.getAnswers())))
                    .build();

            groupThreadDtoList.add(groupThreadDto);
        }
        return groupThreadDtoList;
    }
}