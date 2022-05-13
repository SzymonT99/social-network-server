package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.GroupMemberDto;
import com.server.springboot.domain.dto.response.GroupThreadAnswerDto;
import com.server.springboot.domain.dto.response.GroupThreadAnswerReviewDto;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.ThreadAnswer;
import com.server.springboot.domain.entity.ThreadAnswerReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupThreadAnswerDtoListMapper implements Converter<List<GroupThreadAnswerDto>, List<ThreadAnswer>> {

    private final Converter<GroupMemberDto, GroupMember> groupMemberDtoMapper;
    private final Converter<List<GroupThreadAnswerReviewDto>, List<ThreadAnswerReview>> groupThreadAnswerReviewDtoListMapper;

    @Autowired
    public GroupThreadAnswerDtoListMapper() {
        this.groupMemberDtoMapper = new GroupMemberDtoMapper();
        this.groupThreadAnswerReviewDtoListMapper = new GroupThreadAnswerReviewDtoListMapper();
    }

    @Override
    public List<GroupThreadAnswerDto> convert(List<ThreadAnswer> from) {
        List<GroupThreadAnswerDto> groupThreadAnswerDtoList = new ArrayList<>();

        from = from.stream()
                .sorted(Comparator.comparing(ThreadAnswer::getDate).reversed())
                .collect(Collectors.toList());

        for (ThreadAnswer threadAnswer : from) {
            GroupThreadAnswerDto groupThreadAnswerDto = GroupThreadAnswerDto.builder()
                    .answerId(threadAnswer.getAnswerId())
                    .text(threadAnswer.getText())
                    .averageRating(threadAnswer.getAverageRating())
                    .date(threadAnswer.getDate().toString())
                    .isEdited(threadAnswer.isEdited())
                    .author(groupMemberDtoMapper.convert(threadAnswer.getAnswerAuthor()))
                    .reviews(groupThreadAnswerReviewDtoListMapper.convert(Lists.newArrayList((threadAnswer.getReviews()))))
                    .build();

            groupThreadAnswerDtoList.add(groupThreadAnswerDto);
        }
        return groupThreadAnswerDtoList;
    }
}