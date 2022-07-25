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
public class ChangeUserEmailDto {

    @NotEmpty
    private String oldEmail;

    @NotEmpty
    private String newEmail;

    @NotEmpty
    private String password;
}
