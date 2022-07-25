package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChangeUserPasswordDto {

    @NotEmpty
    private String oldPassword;

    @NotEmpty
    @Size(min = 10, max = 100)
    private String newPassword;

    @NotEmpty
    private String repeatedNewPassword;
}
