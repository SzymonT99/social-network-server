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
public class ChangePhoneNumberDto {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;

    @NotEmpty
    private String newPhoneNumber;
}
