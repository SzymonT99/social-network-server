package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UpdateUserProfileDto {
    private boolean isPublic;
    private String firstName;
    private String lastName;
    private String aboutUser;
    private String gender;
    private String dateOfBirth;
    private String job;
    private String relationshipStatus;
    private String skills;
}
