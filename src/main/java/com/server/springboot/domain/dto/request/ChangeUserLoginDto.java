package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChangeUserLoginDto {
    private String oldLogin;
    private String newLogin;
    private String password;
}
