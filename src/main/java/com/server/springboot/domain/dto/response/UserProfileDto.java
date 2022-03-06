package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.enumeration.RelationshipStatus;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UserProfileDto {
    private Long userProfileId;
    @JsonProperty(value = "isPublic")
    private boolean isPublic;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String phoneNumber;
    private String joinDate;
    private String aboutUser;
    private Gender gender;
    private String dateOfBirth;
    private Integer age;
    private String job;
    private RelationshipStatus relationshipStatus;
    private String skills;
    private ImageDto profilePhoto;
    private AddressDto address;
    private List<SchoolDto> schools;
    private List<WorkPlaceDto> workPlaces;
}

