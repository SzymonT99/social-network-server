package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ProfilePhotoDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserDtoListMapper implements Converter<List<UserDto>, List<User>> {

    private final Converter<ProfilePhotoDto, Image> profilePhotoDtoMapper;

    @Autowired
    public UserDtoListMapper(Converter<ProfilePhotoDto, Image> profilePhotoDtoMapper) {
        this.profilePhotoDtoMapper = profilePhotoDtoMapper;
    }

    @Override
    public List<UserDto> convert(List<User> from) {

        List<UserDto> userDtoList = new ArrayList<>();
        for (User User : from) {
            UserDto userDto = UserDto.builder()
                    .userId(User.getUserId())
                    .activityStatus(User.getActivityStatus())
                    .email(User.getEmail())
                    .firstName(User.getUserProfile().getFirstName())
                    .lastName(User.getUserProfile().getLastName())
                    .profilePhoto(User.getUserProfile().getProfilePhoto() != null ?
                            profilePhotoDtoMapper.convert(User.getUserProfile().getProfilePhoto()) : null)
                    .build();
            userDtoList.add(userDto);
        }
        return userDtoList;
    }
}

