package com.server.springboot.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UpdateUserProfileDto {

    @NotNull
    @JsonProperty(value = "isPublic")
    private boolean isPublic;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    private String aboutUser;

    @NotEmpty
    private String gender;

    @NotEmpty
    private String dateOfBirth;

    private String job;

    private String relationshipStatus;

    private String skills;
}
