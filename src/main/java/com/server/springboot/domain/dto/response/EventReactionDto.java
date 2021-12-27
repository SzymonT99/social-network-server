package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.EventParticipationStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class EventReactionDto {
    private UserDto eventMember;
    private EventParticipationStatus participationStatus;
    private String addedIn;
    private EventDto event;
}
