package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class LikedPostActivityDto {
    private UserDto likedUser;
    private String date;
    private PostDto post;
}
