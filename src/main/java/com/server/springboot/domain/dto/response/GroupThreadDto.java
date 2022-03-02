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
public class GroupThreadDto {
    private Long threadId;
    private String title;
    private String content;
    private ImageDto image;
    private String createdAt;
    private GroupMemberDto author;
    private List<GroupThreadAnswerDto> answers;
}