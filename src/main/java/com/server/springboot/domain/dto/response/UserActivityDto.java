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
public class UserActivityDto {
    private Long userProfileId;
    private List<PostDto> createdPosts;
    private List<PostDto> likes;
    private List<CommentedPostDto> comments;
    private List<SharedPostDto> sharedPosts;
    private List<SharedEventDto> sharedEvents;
    private List<FriendDto> friends;
    private List<GroupDto> groups;
}
