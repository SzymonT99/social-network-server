package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChangeProfilePhotoDto {
    private UserDto user;
    private PostDto changePhotoPost;
}
