package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupMemberDto;
import com.server.springboot.domain.dto.response.GroupThreadAnswerReviewDto;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.ThreadAnswerReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupThreadAnswerReviewDtoListMapper implements Converter<List<GroupThreadAnswerReviewDto>, List<ThreadAnswerReview>> {

    private final Converter<GroupMemberDto, GroupMember> groupMemberDtoMapper;

    @Autowired
    public GroupThreadAnswerReviewDtoListMapper(Converter<GroupMemberDto, GroupMember> groupMemberDtoMapper) {
        this.groupMemberDtoMapper = groupMemberDtoMapper;
    }

    @Override
    public List<GroupThreadAnswerReviewDto> convert(List<ThreadAnswerReview> from) {
        List<GroupThreadAnswerReviewDto> groupThreadAnswerReviewDtoList = new ArrayList<>();

        for (ThreadAnswerReview threadAnswerReview : from) {
            GroupThreadAnswerReviewDto groupThreadAnswerReviewDto = GroupThreadAnswerReviewDto.builder()
                    .answerReviewId(threadAnswerReview.getAnswerReviewId())
                    .rate(threadAnswerReview.getRate())
                    .author(groupMemberDtoMapper.convert(threadAnswerReview.getAnswerReviewAuthor()))
                    .build();

            groupThreadAnswerReviewDtoList.add(groupThreadAnswerReviewDto);
        }
        return groupThreadAnswerReviewDtoList;
    }
}