package com.server.springboot.domain.dto.response;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class LikedSharedPostActivityDto {
    private UserDto likedUser;
    private String date;
    private SharedPostDto sharedPost;
}