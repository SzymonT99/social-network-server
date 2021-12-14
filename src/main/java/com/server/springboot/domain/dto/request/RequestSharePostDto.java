package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestSharePostDto {

    @NotNull
    private Long userId;

    @NotEmpty
    private String text;

    @NotEmpty
    private String isPublic;
}
