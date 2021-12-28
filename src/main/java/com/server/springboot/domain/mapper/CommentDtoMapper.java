package com.server.springboot.domain.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.springboot.domain.dto.response.CommentDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class CommentDtoMapper implements Converter<CommentDto, Comment> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public CommentDtoMapper(Converter<UserDto, User> userDtoMapper) {
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public CommentDto convert(Comment from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return CommentDto.builder()
                .commentId(from.getCommentId())
                .text(from.getText())
                .createdAt(from.getCreatedAt().format(formatter))
                .editedAt(from.getEditedAt() != null
                        ? from.getEditedAt().format(formatter) : null)
                .isEdited(from.isEdited())
                .commentAuthor(userDtoMapper.convert(from.getCommentAuthor()))
                .userLikes(from.getLikes().stream().map(userDtoMapper::convert).collect(Collectors.toList()))
                .build();
    }
}
