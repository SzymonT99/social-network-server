package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ProfilePhotoDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper implements Converter<UserDto, User> {

    private final Converter<ProfilePhotoDto, Image> profilePhotoDtoMapper;

    @Autowired
    public UserDtoMapper(Converter<ProfilePhotoDto, Image> profilePhotoDtoMapper) {
        this.profilePhotoDtoMapper = profilePhotoDtoMapper;
    }

    @Override
    public UserDto convert(User from) {
        return UserDto.builder()
                .userId(from.getUserId())
                .activityStatus(from.getActivityStatus())
                .email(from.getEmail())
                .firstName(from.getUserProfile().getFirstName())
                .lastName(from.getUserProfile().getLastName())
                .profilePhoto(from.getUserProfile().getProfilePhoto() != null ?
                        profilePhotoDtoMapper.convert(from.getUserProfile().getProfilePhoto()) : null)
                .build();
    }
}
