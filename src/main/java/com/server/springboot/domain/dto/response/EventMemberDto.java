package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class EventMemberDto {
    private Long userId;
    private String eventMemberName;
    private String participationStatus;
    private String addedIn;
    private String invitationDate;
    private boolean invitationDisplayed;
}

