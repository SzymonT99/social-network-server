package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.enumeration.GroupPermissionType;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupMemberDto {
    private Long groupMemberId;
    private String groupMemberName;
    private GroupPermissionType groupPermissionType;
    private GroupMemberStatus groupMemberStatus;
    private LocalDateTime addedIn;
    private boolean invitationDisplayed;
}
