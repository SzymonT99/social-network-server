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
public class SharedPostDto {
    private Long sharedPostId;
    private Long sharingId;
    private String sharingText;
    private String sharingDate;
    private UserDto authorOfSharing;
    @JsonProperty(value = "isPublic")
    private boolean isPublic;
    @JsonProperty(value = "isCommentingBlocked")
    private boolean isCommentingBlocked;
    private List<LikedPostDto> sharingLikes;
    private List<CommentDto> sharingComments;
    private PostDto sharedPost;
}
