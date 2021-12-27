package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.LikedPostActivityDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.LikedPost;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class LikedPostActivityDtoMapper implements Converter<LikedPostActivityDto, LikedPost> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<PostDto, Post> postDtoMapper;

    @Autowired
    public LikedPostActivityDtoMapper(Converter<UserDto, User> userDtoMapper, Converter<PostDto, Post> postDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.postDtoMapper = postDtoMapper;
    }

    @Override
    public LikedPostActivityDto convert(LikedPost from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return LikedPostActivityDto.builder()
                .likedUser(userDtoMapper.convert(from.getLikedPostUser()))
                .date(from.getDate().format(formatter))
                .post(postDtoMapper.convert(from.getPost()))
                .build();
    }
}