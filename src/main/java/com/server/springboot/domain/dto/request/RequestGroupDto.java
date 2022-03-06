package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestGroupDto {

    @NotEmpty
    @Size(max = 100)
    private String name;

    @NotEmpty
    private String description;

    @NotEmpty
    private String isPublic;
}
