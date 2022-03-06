package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ChangeProfilePhotoDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ChangeProfilePhotoDtoMapper implements Converter<ChangeProfilePhotoDto, Image> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<PostDto, Post> postDtoMapper;

    @Autowired
    public ChangeProfilePhotoDtoMapper(Converter<UserDto, User> userDtoMapper,
                                       Converter<PostDto, Post> postDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.postDtoMapper = postDtoMapper;
    }

    @Override
    public ChangeProfilePhotoDto convert(Image from) {
        return ChangeProfilePhotoDto.builder()
                .user(userDtoMapper.convert(from.getUserProfile().getUser()))
                .changePhotoPost(postDtoMapper.convert(from.getChangePhotoPost()))
                .build();
    }
}

