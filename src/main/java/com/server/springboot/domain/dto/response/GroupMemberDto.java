package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.enumeration.GroupPermissionType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupMemberDto {
    private Long groupMemberId;
    private UserDto user;
    private AddressDto address;
    private GroupPermissionType groupPermissionType;
    private GroupMemberStatus groupMemberStatus;
    private String addedIn;
    private boolean invitationDisplayed;
}