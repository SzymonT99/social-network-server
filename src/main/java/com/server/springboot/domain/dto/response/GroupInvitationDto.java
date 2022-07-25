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
public class GroupInvitationDto {
    private boolean invitationDisplayed;
    private String invitationDate;
    private Long groupId;
    private String groupName;
    private ImageDto groupImage;
    private UserDto groupCreator;
    private String groupCreatedAt;
    private List<GroupMemberDto> groupMembers;
    private Integer groupPostsNumber;
    private List<InterestDto> groupInterests;
}
