package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.LikedPost;
import com.server.springboot.domain.entity.SharedPost;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class LikedSharedPostActivityDtoMapper implements Converter<LikedSharedPostActivityDto, LikedPost> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<SharedPostDto, SharedPost> sharedPostDtoMapper;

    @Autowired
    public LikedSharedPostActivityDtoMapper(Converter<UserDto, User> userDtoMapper,
                                            Converter<SharedPostDto, SharedPost> sharedPostDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.sharedPostDtoMapper = sharedPostDtoMapper;
    }

    @Override
    public LikedSharedPostActivityDto convert(LikedPost from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return LikedSharedPostActivityDto.builder()
                .likedUser(userDtoMapper.convert(from.getLikedPostUser()))
                .date(from.getDate().format(formatter))
                .sharedPost(sharedPostDtoMapper.convert(from.getPost().getSharedNewPost()))
                .build();
    }
}