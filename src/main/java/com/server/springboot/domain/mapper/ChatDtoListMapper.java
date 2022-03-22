package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Chat;
import com.server.springboot.domain.entity.ChatMember;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatDtoListMapper implements Converter<List<ChatDto>, List<Chat>> {

    private final Converter<List<ChatMemberDto>, List<ChatMember>> chatMemberDtoListMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<ImageDto, Image> imageDtoMapper;

    @Autowired
    public ChatDtoListMapper(Converter<List<ChatMemberDto>, List<ChatMember>> chatMemberDtoListMapper,
                             Converter<UserDto, User> userDtoMapper,
                             Converter<ImageDto, Image> imageDtoMapper) {
        this.chatMemberDtoListMapper = chatMemberDtoListMapper;
        this.userDtoMapper = userDtoMapper;
        this.imageDtoMapper = imageDtoMapper;
    }

    @Override
    public List<ChatDto> convert(List<Chat> from) {
        List<ChatDto> chatDtoList = new ArrayList<>();

        for (Chat chat : from) {
            ChatDto chatDto = ChatDto.builder()
                    .chatId(chat.getChatId())
                    .name(chat.getName())
                    .createdAt(chat.getCreatedAt().toString())
                    .chatCreator(userDtoMapper.convert(chat.getChatCreator()))
                    .chatMembers(chatMemberDtoListMapper.convert(Lists.newArrayList(chat.getChatMembers())))
                    .image(chat.getImage() != null ? imageDtoMapper.convert(chat.getImage()) : null)
                    .build();

            chatDtoList.add(chatDto);
        }
        return chatDtoList;
    }
}
