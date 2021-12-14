package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.entity.Post;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PostMapper implements Converter<Post, RequestPostDto> {

    @Override
    public Post convert(RequestPostDto from) {
        return Post.builder()
                .text(from.getText())
                .isPublic(Boolean.parseBoolean(from.getIsPublic()))
                .createdAt(LocalDateTime.now())
                .isEdited(false)
                .isDeleted(false)
                .build();
    }
}

