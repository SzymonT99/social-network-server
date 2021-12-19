package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserProfileDtoMapper implements Converter<UserProfileDto, UserProfile> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<AddressDto, Address> addressDtoMapper;
    private final Converter<List<SchoolDto>, List<School>> schoolDtoMapper;
    private final Converter<List<WorkPlaceDto>, List<WorkPlace>> workPlaceDtoListMapper;

    @Autowired
    public UserProfileDtoMapper(Converter<ImageDto, Image> imageDtoMapper,
                                Converter<AddressDto, Address> addressDtoMapper,
                                Converter<List<SchoolDto>, List<School>> schoolDtoMapper,
                                Converter<List<WorkPlaceDto>, List<WorkPlace>> workPlaceDtoListMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.addressDtoMapper = addressDtoMapper;
        this.schoolDtoMapper = schoolDtoMapper;
        this.workPlaceDtoListMapper = workPlaceDtoListMapper;
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
                .profilePhoto(from.getProfilePhoto() != null ? imageDtoMapper.convert(from.getProfilePhoto()) : null)
                .address(from.getAddress() != null ? addressDtoMapper.convert(from.getAddress()) : null)
                .schools(schoolDtoMapper.convert(Lists.newArrayList(from.getSchools())))
                .workPlaces(workPlaceDtoListMapper.convert(Lists.newArrayList(from.getWorkPlaces())))
                .build();
    }
}





