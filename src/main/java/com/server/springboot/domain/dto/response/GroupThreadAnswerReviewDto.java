package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupThreadAnswerReviewDto {
    private Long answerReviewId;
    private Float rate;
    private GroupMemberDto author;
}