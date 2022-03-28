package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatDetailsDtoMapper implements Converter<ChatDetailsDto, Chat>{

    private final Converter<List<ChatMemberDto>, List<ChatMember>> chatMemberDtoListMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<List<ChatMessageDto>, List<ChatMessage>> chatMessageDtoListMapper;

    @Autowired
    public ChatDetailsDtoMapper(Converter<List<ChatMemberDto>, List<ChatMember>> chatMemberDtoListMapper,
                                Converter<UserDto, User> userDtoMapper,
                                Converter<ImageDto, Image> imageDtoMapper,
                                Converter<List<ChatMessageDto>, List<ChatMessage>> chatMessageDtoListMapper) {
        this.chatMemberDtoListMapper = chatMemberDtoListMapper;
        this.userDtoMapper = userDtoMapper;
        this.imageDtoMapper = imageDtoMapper;
        this.chatMessageDtoListMapper = chatMessageDtoListMapper;
    }

    @Override
    public ChatDetailsDto convert(Chat from) {
        return ChatDetailsDto.builder()
                .chatId(from.getChatId())
                .name(from.getName())
                .createdAt(from.getCreatedAt().toString())
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .chatCreator(from.getChatCreator() != null ? userDtoMapper.convert(from.getChatCreator()) : null)
                .isPrivate(from.isPrivate())
                .chatMembers(chatMemberDtoListMapper.convert(Lists.newArrayList(from.getChatMembers())))
                .messages(chatMessageDtoListMapper.convert(Lists.newArrayList(from.getChatMessages())))
                .build();
    }
}