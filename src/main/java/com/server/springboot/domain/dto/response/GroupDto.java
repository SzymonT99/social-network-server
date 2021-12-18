package com.server.springboot.domain.dto.response;

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
}