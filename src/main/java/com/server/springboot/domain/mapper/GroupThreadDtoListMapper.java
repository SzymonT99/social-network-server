package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.GroupThread;
import com.server.springboot.domain.entity.ThreadAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupThreadDtoListMapper implements Converter<List<GroupThreadDto>, List<GroupThread>> {

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
    public List<GroupThreadDto> convert(List<GroupThread> from) {
        List<GroupThreadDto> groupThreadDtoList = new ArrayList<>();

        from = from.stream()
                .sorted(Comparator.comparing(GroupThread::getCreatedAt).reversed())
                .collect(Collectors.toList());

        for (GroupThread groupThread : from) {
            GroupThreadDto groupThreadDto = GroupThreadDto.builder()
                    .threadId(groupThread.getThreadId())
                    .title(groupThread.getTitle())
                    .content(groupThread.getContent())
                    .image(groupThread.getImage() != null ? imageDtoMapper.convert(groupThread.getImage()) : null)
                    .isEdited(groupThread.isEdited())
                    .createdAt(groupThread.getCreatedAt().toString())
                    .author(groupMemberDtoMapper.convert(groupThread.getThreadAuthor()))
                    .answers(groupThreadAnswerDtoListMapper.convert(Lists.newArrayList(groupThread.getAnswers())))
                    .build();

            groupThreadDtoList.add(groupThreadDto);
        }
        return groupThreadDtoList;
    }
}