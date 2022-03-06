package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.GroupMemberStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupJoiningDto {
    private UserDto userMember;
    private GroupMemberStatus groupMemberStatus;
    private String addedIn;
    private GroupDto group;
}