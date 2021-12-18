package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class UserProfileDtoMapper implements Converter<UserProfileDto, UserProfile> {

    private final Converter<ImageDto, Image> imageDtoMapper;

    @Autowired
    public UserProfileDtoMapper(Converter<ImageDto, Image> imageDtoMapper) {
        this.imageDtoMapper = imageDtoMapper;
    }

    @Override
    public UserProfileDto convert(UserProfile from) {
        DateTimeFormatter formatterWithoutTime = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return UserProfileDto.builder()
                .userProfileId(from.getUserProfileId())
                .isPublic(from.isPublic())
                .firstName(from.getFirstName())
                .lastName(from.getLastName())
                .aboutUser(from.getAboutUser())
                .gender(from.getGender())
                .dateOfBirth(from.getDateOfBirth().format(formatterWithoutTime))
                .age(from.getAge())
                .job(from.getJob())
                .relationshipStatus(from.getRelationshipStatus())
                .skills(from.getSkills())
                .profilePhoto(imageDtoMapper.convert(from.getProfilePhoto()))
                .address(AddressDto.builder()
                        .addressId(from.getAddress().getAddressId())
                        .country(from.getAddress().getCountry())
                        .city(from.getAddress().getCity())
                        .street(from.getAddress().getStreet())
                        .zipCode(from.getAddress().getZipCode())
                        .build())
                .schools(
                        from.getSchools().stream()
                                .map(school -> SchoolDto.builder()
                                        .schoolId(school.getSchoolId())
                                        .schoolType(school.getSchoolType())
                                        .name(school.getName())
                                        .startDate(school.getStartDate().format(formatterWithoutTime))
                                        .graduationDate(school.getGraduationDate() != null
                                                ? school.getGraduationDate().format(formatterWithoutTime)
                                                : null)
                                        .build())
                                .collect(Collectors.toList())
                )
                .workPlaces(
                        from.getWorkPlaces().stream()
                                .map(workPlace -> WorkPlaceDto.builder()
                                        .workPlaceId(workPlace.getWorkPlaceId())
                                        .company(workPlace.getCompany())
                                        .position(workPlace.getPosition())
                                        .startDate(workPlace.getStartDate().format(formatterWithoutTime))
                                        .endDate(workPlace.getEndDate() != null
                                                ? workPlace.getEndDate().format(formatterWithoutTime)
                                                : null)
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}





