package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
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
    private String name;
    private ImageDto image;
    private String description;
    private String createdAt;
    @JsonProperty(value = "isPublic")
    private boolean isPublic;
    private UserDto groupCreator;
    private List<GroupRuleDto> rules;
    private List<GroupMemberDto> members;
    private List<PostDto> posts;
    private List<InterestDto> interests;
}