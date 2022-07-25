package com.server.springboot.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @NotEmpty
    private String text;

    @JsonProperty
    private boolean isPublic;

    @JsonProperty
    private boolean isCommentingBlocked;
}
