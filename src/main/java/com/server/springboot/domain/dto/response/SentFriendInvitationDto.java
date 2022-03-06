package com.server.springboot.domain.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class SentFriendInvitationDto {
    private Long friendId;
    private Boolean isInvitationAccepted;
    private Boolean invitationDisplayed;
    private String invitationDate;
    private UserDto invitedUser;
    private List<UserDto> invitedUserFriends;
}
