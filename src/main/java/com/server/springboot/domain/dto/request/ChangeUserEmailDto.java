package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChangeUserEmailDto {
    private String oldEmail;
    private String newEmail;
    private String password;
}
