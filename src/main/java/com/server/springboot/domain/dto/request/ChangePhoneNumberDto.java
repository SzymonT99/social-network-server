package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChangePhoneNumberDto {
    private String login;
    private String password;
    private String newPhoneNumber;
}
