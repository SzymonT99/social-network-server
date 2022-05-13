package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ChatMemberDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.ChatMember;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMemberDtoListMapper implements Converter<List<ChatMemberDto>, List<ChatMember>> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public ChatMemberDtoListMapper() {
        this.userDtoMapper = new UserDtoMapper();
    }

    @Override
    public List<ChatMemberDto> convert(List<ChatMember> from) {
        List<ChatMemberDto> chatMemberDtoList = new ArrayList<>();

        from = from.stream()
                .sorted(Comparator.comparing(ChatMember::getChatMemberId))
                .collect(Collectors.toList());

        for (ChatMember chatMember : from) {
            ChatMemberDto chatMemberDto = ChatMemberDto.builder()
                    .chatMemberId(chatMember.getChatMemberId())
                    .user(userDtoMapper.convert(chatMember.getUserMember()))
                    .addedIn(chatMember.getAddedIn().toString())
                    .hasMutedChat(chatMember.isHasMutedChat())
                    .canAddOthers(chatMember.getCanAddOthers() != null ? chatMember.getCanAddOthers() : null)
                    .build();

            chatMemberDtoList.add(chatMemberDto);
        }
        return chatMemberDtoList;
    }
}