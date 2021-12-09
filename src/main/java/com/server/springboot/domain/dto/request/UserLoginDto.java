package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UserLoginDto {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;
}
