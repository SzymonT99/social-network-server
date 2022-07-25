package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.CommentDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class CommentDtoMapper implements Converter<CommentDto, Comment> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public CommentDtoMapper() {
        this.userDtoMapper = new UserDtoMapper();
    }

    @Override
    public CommentDto convert(Comment from) {
        return CommentDto.builder()
                .commentId(from.getCommentId())
                .text(from.getText())
                .createdAt(from.getCreatedAt().toString())
                .editedAt(from.getEditedAt() != null
                        ? from.getEditedAt().toString(): null)
                .isEdited(from.isEdited())
                .commentAuthor(userDtoMapper.convert(from.getCommentAuthor()))
                .userLikes(from.getLikes() != null ? from.getLikes().stream().map(userDtoMapper::convert).collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}
