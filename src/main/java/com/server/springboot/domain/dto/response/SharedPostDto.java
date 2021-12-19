package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class SharedPostDto {
    private Long userId;
    private String authorOfSharing;
    private String sharingText;
    private String sharingDate;
    @JsonProperty(value = "isPublic")
    private boolean isPublic;
    @JsonProperty(value = "isCommentingBlocked")
    private boolean isCommentingBlocked;
    private PostDto sharedPost;
}
