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
    public UserDtoListMapper() {
        this.profilePhotoDtoMapper = new ProfilePhotoDtoMapper();
    }

    @Override
    public List<UserDto> convert(List<User> from) {

        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : from) {
            UserDto userDto = UserDto.builder()
                    .userId(user.getUserId())
                    .activityStatus(user.getActivityStatus())
                    .email(user.getEmail())
                    .firstName(user.getUserProfile().getFirstName())
                    .lastName(user.getUserProfile().getLastName())
                    .profilePhoto(user.getUserProfile().getProfilePhoto() != null ?
                            profilePhotoDtoMapper.convert(user.getUserProfile().getProfilePhoto()) : null)
                    .build();
            userDtoList.add(userDto);
        }
        return userDtoList;
    }
}

