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
public class UserDetailsDto {
    private Long userId;
    private String email;
    private String createdAt;
    private ActivityStatus activityStatus;
    private UserProfileDto userProfile;
}
