package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.ChatDto;
import com.server.springboot.domain.dto.response.ChatMemberDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Chat;
import com.server.springboot.domain.entity.ChatMember;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatDtoMapper implements Converter<ChatDto, Chat> {

    private final Converter<List<ChatMemberDto>, List<ChatMember>> chatMemberDtoListMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<ImageDto, Image> imageDtoMapper;

    @Autowired
    public ChatDtoMapper() {
        this.chatMemberDtoListMapper = new ChatMemberDtoListMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.imageDtoMapper = new ImageDtoMapper();
    }

    @Override
    public ChatDto convert(Chat from) {
        return ChatDto.builder()
                .chatId(from.getChatId())
                .name(from.getName())
                .createdAt(from.getCreatedAt().toString())
                .chatCreator(from.getChatCreator() != null ? userDtoMapper.convert(from.getChatCreator()) : null)
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .isPrivate(from.isPrivate())
                .members(chatMemberDtoListMapper.convert(Lists.newArrayList(from.getChatMembers())))
                .build();

    }
}