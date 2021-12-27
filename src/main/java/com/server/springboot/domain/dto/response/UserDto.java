package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.Gender;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UserDto {
    private Long userId;
    private ActivityStatus activityStatus;
    private String email;
    private String firstName;
    private String lastName;
    private ProfilePhotoDto profilePhoto;
}
