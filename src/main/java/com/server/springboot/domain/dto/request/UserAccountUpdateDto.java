package com.server.springboot.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UserAccountUpdateDto {

    @NotBlank
    @Size(min = 6, max = 20)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @JsonProperty(value = "isPublicProfile")
    @NotNull
    private boolean isPublicProfile;

    @NotBlank
    @Size(min = 9)
    private String phoneNumber;

    private Integer incorrectLoginCounter;

    @NotNull
    private boolean activateAccount;

    @JsonProperty(value = "isBlocked")
    @NotNull
    private boolean isBlocked;

    @JsonProperty(value = "isBanned")
    @NotNull
    private boolean isBanned;
}
