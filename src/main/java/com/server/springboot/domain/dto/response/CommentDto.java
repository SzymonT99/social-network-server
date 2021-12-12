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
public class CommentDto {
    private Long commentId;
    private String text;
    private String date;
    private String authorName;
    private List<String> userLikes;
}
