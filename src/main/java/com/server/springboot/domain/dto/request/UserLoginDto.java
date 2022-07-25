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
    @Size(min = 6, max = 20)
    private String login;

    @NotEmpty
    @Size(min = 10, max = 100)
    private String password;
}
