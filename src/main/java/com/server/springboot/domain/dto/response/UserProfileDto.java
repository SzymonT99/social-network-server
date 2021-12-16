package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.enumeration.RelationshipStatus;
import lombok.*;

import java.time.LocalDate;
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
    private boolean isPublic;
    private String firstName;
    private String lastName;
    private String aboutUser;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Integer age;
    private String job;
    private RelationshipStatus relationshipStatus;
    private String skills;
    private AddressDto address;
    private List<SchoolDto> schools;
    private List<UserFavouriteDto> favourites;
    private List<WorkPlaceDto> workPlaces;
    private List<ImageDto> userImages;
}