package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Float averageRating;
    private String date;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
    private GroupMemberDto author;
    private List<GroupThreadAnswerReviewDto> reviews;
}