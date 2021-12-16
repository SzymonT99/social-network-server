package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupDetailsDto {
    private Long groupId;
    private Long groupCreatorId;
    private String groupCreatorName;
    private String name;
    private ImageDto image;
    private String description;
    private LocalDateTime createdAt;
    private List<GroupRuleDto> rules;
    private List<GroupMemberDto> groupMembers;
    private List<Post> groupPosts;
    private List<Interest> groupInterests;
}
