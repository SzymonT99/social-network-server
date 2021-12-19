package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.UserDetailsDto;
import com.server.springboot.domain.dto.response.UserProfileDto;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.entity.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class UserDetailsDtoMapper implements Converter<UserDetailsDto, User> {

    private final Converter<UserProfileDto, UserProfile> userProfileDtoMapper;

    @Autowired
    public UserDetailsDtoMapper(Converter<UserProfileDto, UserProfile> userProfileDtoMapper) {
        this.userProfileDtoMapper = userProfileDtoMapper;
    }

    @Override
    public UserDetailsDto convert(User from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return UserDetailsDto.builder()
                .userId(from.getUserId())
                .email(from.getEmail())
                .createdAt(from.getCreatedAt().format(formatter))
                .activityStatus(from.getActivityStatus())
                .userProfile(userProfileDtoMapper.convert(from.getUserProfile()))
                .build();
    }
}
