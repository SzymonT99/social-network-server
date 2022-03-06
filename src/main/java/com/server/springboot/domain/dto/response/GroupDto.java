package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupDto {
    private Long groupId;
    private String name;
    private ImageDto image;
    private String description;
    private String createdAt;
    @JsonProperty(value = "isPublic")
    private boolean isPublic;
    private UserDto groupCreator;
    private Long membersNumber;
    private Long postsNumber;
}