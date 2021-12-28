package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.SharedPost;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class SharedPostDtoMapper implements Converter<SharedPostDto, SharedPost> {

    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public SharedPostDtoMapper(Converter<PostDto, Post> postDtoMapper, Converter<UserDto, User> userDtoMapper) {
        this.postDtoMapper = postDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public SharedPostDto convert(SharedPost from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return SharedPostDto.builder()
                .authorOfSharing(userDtoMapper.convert(from.getSharedPostUser()))
                .sharingText(from.getNewPost().getText())
                .sharingDate(from.getDate().format(formatter))
                .isPublic(from.getNewPost().isPublic())
                .isCommentingBlocked(from.getNewPost().isCommentingBlocked())
                .sharedPost(postDtoMapper.convert(from.getBasePost()))
                .build();
    }
}
