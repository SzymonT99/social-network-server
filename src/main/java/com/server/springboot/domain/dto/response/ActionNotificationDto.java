package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.ActionType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ActionNotificationDto {
    private UserDto author;
    private ActionType actionType;
    private String date;
}
