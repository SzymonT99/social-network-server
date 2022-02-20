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
public class FriendDto {
    private Long friendId;
    private Boolean isInvitationAccepted;
    private String invitationDate;
    private String friendFromDate;
    private AddressDto address;
    private UserDto user;
    private List<UserDto> userFriends;
}