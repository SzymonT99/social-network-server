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
    public ChatDtoListMapper() {
        this.chatMemberDtoListMapper = new ChatMemberDtoListMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.imageDtoMapper = new ImageDtoMapper();
    }

    @Override
    public List<ChatDto> convert(List<Chat> from) {
        List<ChatDto> chatDtoList = new ArrayList<>();

        for (Chat chat : from) {

            ChatDto chatDto = ChatDto.builder()
                    .chatId(chat.getChatId())
                    .name(chat.getName())
                    .createdAt(chat.getCreatedAt().toString())
                    .chatCreator(chat.getChatCreator() != null ? userDtoMapper.convert(chat.getChatCreator()) : null)
                    .image(chat.getImage() != null ? imageDtoMapper.convert(chat.getImage()) : null)
                    .isPrivate(chat.isPrivate())
                    .members(chatMemberDtoListMapper.convert(Lists.newArrayList(chat.getChatMembers())))
                    .build();

            chatDtoList.add(chatDto);
        }
        return chatDtoList;
    }
}
