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
public class PostDto {
    private Long postId;
    private String postAuthor;
    private String text;
    private List<ImageDto> images;
    private String createdAt;
    private String editedAt;
    private boolean isPublic;
    private boolean isEdited;
    private List<LikedPostDto> likes;
    private List<CommentDto> comments;
}

