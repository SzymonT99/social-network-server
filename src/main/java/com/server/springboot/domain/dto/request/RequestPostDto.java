package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestPostDto {

    @NotEmpty
    private String text;

    @NotEmpty
    private String isPublic;

    @NotEmpty
    private String isCommentingBlocked;
}
