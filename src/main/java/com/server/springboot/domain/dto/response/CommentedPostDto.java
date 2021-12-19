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
    private String commentAuthor;
    private String commentText;
    private Long postId;
    private Long postAuthorId;
    private String postAuthor;
    private String postText;
    private List<ImageDto> postImages;
    private String createdAt;
    private String editedAt;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
}
