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
public class CommentedPostDto {
    private Long commentId;
    private String commentText;
    private UserDto commentAuthor;
    private PostDto commentedPost;
    private SharedPostDto commentedSharedPost;
    private String createdAt;
    private String editedAt;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
}
