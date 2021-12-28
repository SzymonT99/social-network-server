package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.CommentActivityDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class CommentActivityDtoMapper implements Converter<CommentActivityDto, Comment> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<PostDto, Post> postDtoMapper;

    @Autowired
    public CommentActivityDtoMapper(Converter<UserDto, User> userDtoMapper, Converter<PostDto, Post> postDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.postDtoMapper = postDtoMapper;
    }

    @Override
    public CommentActivityDto convert(Comment from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return CommentActivityDto.builder()
                .commentId(from.getCommentId())
                .text(from.getText())
                .createdAt(from.getCreatedAt().format(formatter))
                .editedAt(from.getEditedAt() != null
                        ? from.getEditedAt().format(formatter) : null)
                .isEdited(from.isEdited())
                .commentAuthor(userDtoMapper.convert(from.getCommentAuthor()))
                .userLikes(from.getLikes().stream().map(userDtoMapper::convert).collect(Collectors.toList()))
                .post(postDtoMapper.convert(from.getCommentedPost()))
                .build();
    }
}
