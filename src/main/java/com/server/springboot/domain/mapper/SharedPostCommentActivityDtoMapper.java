package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.SharedPost;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class SharedPostCommentActivityDtoMapper implements Converter<SharedPostCommentActivityDto, Comment> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<SharedPostDto, SharedPost> sharedPostDtoMapper;

    @Autowired
    public SharedPostCommentActivityDtoMapper(Converter<UserDto, User> userDtoMapper,
                                              Converter<SharedPostDto, SharedPost> sharedPostDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.sharedPostDtoMapper = sharedPostDtoMapper;
    }

    @Override
    public SharedPostCommentActivityDto convert(Comment from) {

        return SharedPostCommentActivityDto.builder()
                .commentId(from.getCommentId())
                .text(from.getText())
                .createdAt(from.getCreatedAt().toString())
                .editedAt(from.getEditedAt() != null
                        ? from.getEditedAt().toString() : null)
                .isEdited(from.isEdited())
                .commentAuthor(userDtoMapper.convert(from.getCommentAuthor()))
                .userLikes(from.getLikes().stream().map(userDtoMapper::convert).collect(Collectors.toList()))
                .sharedPost(sharedPostDtoMapper.convert(from.getCommentedPost().getSharedNewPost()))
                .build();
    }
}