package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ChatMessageDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.ChatMessage;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageDtoMapper implements Converter<ChatMessageDto, ChatMessage> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public ChatMessageDtoMapper(Converter<ImageDto, Image> imageDtoMapper, Converter<UserDto, User> userDtoMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public ChatMessageDto convert(ChatMessage from) {
        return ChatMessageDto.builder()
                .messageId(from.getMessageId())
                .text(from.getText())
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .createdAt(from.getCreatedAt().toString())
                .editedAt(from.getEditedAt() != null ? from.getEditedAt().toString() : null)
                .isEdited(from.isEdited())
                .isDeleted(from.isDeleted())
                .author(userDtoMapper.convert(from.getMessageAuthor()))
                .build();

    }
}
