package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.ActivityStatus;
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
    private String email;
    private ActivityStatus activityStatus;
    private UserProfileDto userProfile;
}
