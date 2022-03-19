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
public class GroupThreadDto {
    private Long threadId;
    private String title;
    private String content;
    private ImageDto image;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
    private String createdAt;
    private GroupMemberDto author;
    private List<GroupThreadAnswerDto> answers;
}