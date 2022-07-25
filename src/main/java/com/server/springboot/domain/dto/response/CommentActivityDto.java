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
public class CommentActivityDto {
    private Long commentId;
    private String text;
    private String createdAt;
    private String editedAt;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
    private UserDto commentAuthor;
    private List<UserDto> userLikes;
    private PostDto post;
}
