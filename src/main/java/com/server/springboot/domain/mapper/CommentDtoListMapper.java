package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.CommentDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentDtoListMapper implements Converter<List<CommentDto>, List<Comment>> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public CommentDtoListMapper() {
        this.userDtoMapper = new UserDtoMapper();
    }

    @Override
    public List<CommentDto> convert(List<Comment> from) {
        List<CommentDto> commentDtoList = new ArrayList<>();
        for (Comment comment : from) {
            CommentDto commentDto = CommentDto.builder()
                    .commentId(comment.getCommentId())
                    .text(comment.getText())
                    .createdAt(comment.getCreatedAt().toString())
                    .editedAt(comment.getEditedAt() != null
                            ? comment.getEditedAt().toString(): null)
                    .isEdited(comment.isEdited())
                    .commentAuthor(userDtoMapper.convert(comment.getCommentAuthor()))
                    .userLikes(comment.getLikes().stream().map(userDtoMapper::convert).collect(Collectors.toList()))
                    .build();

            commentDtoList.add(commentDto);
        }
        return commentDtoList;
    }
}