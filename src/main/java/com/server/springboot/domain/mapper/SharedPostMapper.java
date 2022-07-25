package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.entity.Post;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SharedPostMapper implements Converter<Post, RequestSharePostDto> {

    @Override
    public Post convert(RequestSharePostDto from) {
        return Post.builder()
                .text(from.getText())
                .isPublic(from.isPublic())
                .createdAt(LocalDateTime.now())
                .isCommentingBlocked(from.isCommentingBlocked())
                .isEdited(false)
                .isDeleted(false)
                .build();
    }
}
