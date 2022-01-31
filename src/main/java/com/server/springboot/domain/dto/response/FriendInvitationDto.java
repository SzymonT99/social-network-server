package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class FriendInvitationDto {
    private Long friendId;
    private Boolean isInvitationAccepted;
    private Boolean invitationDisplayed;
    private String invitationDate;
    private UserDto invitingUser;
    private Integer friendsNumber;
}
