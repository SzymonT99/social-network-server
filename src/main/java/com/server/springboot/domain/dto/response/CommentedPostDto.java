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
    private Long postId;
    private String postText;
    private UserDto postAuthor;
    private List<ImageDto> postImages;
    private String createdAt;
    private String editedAt;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
}
