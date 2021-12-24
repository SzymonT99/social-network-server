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
    private String oldPhoneNumber;

    @NotEmpty
    private String newPhoneNumber;

    @NotEmpty
    private String password;
}
