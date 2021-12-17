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
public class PostDto {
    private Long postId;
    private Long postAuthorId;
    private String postAuthor;
    private String text;
    private List<ImageDto> images;
    private String createdAt;
    private String editedAt;
    @JsonProperty(value = "isPublic")
    private boolean isPublic;
    @JsonProperty(value = "isCommentingBlocked")
    private boolean isCommentingBlocked;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
    private List<LikedPostDto> likes;
    private List<CommentDto> comments;
    private List<SharedPostInfoDto> sharing;
}

