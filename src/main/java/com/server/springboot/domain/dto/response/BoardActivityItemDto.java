package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.ActivityType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class BoardActivityItemDto {
    private String activityDate;
    private ActivityType activityType;
    private UserDto activityAuthor;
    private Object activity;
}
