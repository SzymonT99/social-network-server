package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class FriendDto {
    private Long friendId;
    private boolean isInvitationAccepted;
    private boolean invitationDisplayed;
    private String invitationDate;
    private String friendFromDate;
    private UserDto user;
}
