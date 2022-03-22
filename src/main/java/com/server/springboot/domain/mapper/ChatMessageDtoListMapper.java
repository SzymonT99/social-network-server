package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ChatMessageDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.ChatMessage;
import com.server.springboot.domain.entity.GroupThread;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMessageDtoListMapper implements Converter<List<ChatMessageDto>, List<ChatMessage>> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<ImageDto, Image> imageDtoMapper;

    @Autowired
    public ChatMessageDtoListMapper(Converter<UserDto, User> userDtoMapper, Converter<ImageDto, Image> imageDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.imageDtoMapper = imageDtoMapper;
    }

    @Override
    public List<ChatMessageDto> convert(List<ChatMessage> from) {
        List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();

        from = from.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .collect(Collectors.toList());

        for (ChatMessage chatMessage : from) {
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .messageId(chatMessage.getMessageId())
                    .text(chatMessage.getText())
                    .image(chatMessage.getImage() != null ? imageDtoMapper.convert(chatMessage.getImage()) : null)
                    .createdAt(chatMessage.getCreatedAt().toString())
                    .editedAt(chatMessage.getEditedAt() != null ? chatMessage.getEditedAt().toString() : null)
                    .isEdited(chatMessage.isEdited())
                    .isDeleted(chatMessage.isDeleted())
                    .author(userDtoMapper.convert(chatMessage.getMessageAuthor()))
                    .build();

            chatMessageDtoList.add(chatMessageDto);
        }
        return chatMessageDtoList;
    }
}
