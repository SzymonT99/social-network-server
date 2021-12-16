package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestGroupDto {
    private String name;
    private String description;
    private String groupCreatorId;
    private String groupCreatorName;
}
