package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.entity.Comment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CommentMapper implements Converter<Comment, RequestCommentDto> {

    @Override
    public Comment convert(RequestCommentDto from) {
        return Comment.builder()
                .text(from.getCommentText())
                .createdAt(LocalDateTime.now())
                .isEdited(false)
                .build();
    }
}
