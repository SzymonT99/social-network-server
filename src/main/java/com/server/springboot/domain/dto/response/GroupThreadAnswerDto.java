package com.server.springboot.domain.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupThreadAnswerDto {
    private Long answerId;
    private String text;
    private Float averageRate;
    private String date;
    private GroupMemberDto author;
    private List<GroupThreadAnswerReviewDto> reviews;
}