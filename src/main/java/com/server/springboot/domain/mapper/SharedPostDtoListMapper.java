package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.SharedPost;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class SharedPostDtoListMapper implements Converter<List<SharedPostDto>, List<SharedPost>> {

    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public SharedPostDtoListMapper(Converter<PostDto, Post> postDtoMapper, Converter<UserDto, User> userDtoMapper) {
        this.postDtoMapper = postDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<SharedPostDto> convert(List<SharedPost> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        List<SharedPostDto> sharedPostDtoList = new ArrayList<>();

        for (SharedPost sharedPost : from) {
            SharedPostDto sharedPostDto = SharedPostDto.builder()
                    .authorOfSharing(userDtoMapper.convert(sharedPost.getSharedPostUser()))
                    .sharingText(sharedPost.getNewPost().getText())
                    .sharingDate(sharedPost.getDate().format(formatter))
                    .isPublic(sharedPost.getNewPost().isPublic())
                    .isCommentingBlocked(sharedPost.getNewPost().isCommentingBlocked())
                    .sharedPost(postDtoMapper.convert(sharedPost.getBasePost()))
                    .build();
            sharedPostDtoList.add(sharedPostDto);
        }

        return sharedPostDtoList;
    }
}
