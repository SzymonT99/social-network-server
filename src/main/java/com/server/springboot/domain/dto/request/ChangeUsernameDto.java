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
public class ChangeUsernameDto {

    @NotEmpty
    private String oldUsername;

    @NotEmpty
    @Size(min = 6, max = 20)
    private String newUsername;

    @NotEmpty
    private String password;
}
