package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.NotificationType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class NotificationDto {
    private NotificationType notificationType;
    private String notificationDate;
    private boolean isNotificationDisplayed;
    private UserDto activityInitiator;
    private Object details;
}
