package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChangeUserPasswordDto {
    private String login;
    private String oldPassword;
    private String newPassword;
}
