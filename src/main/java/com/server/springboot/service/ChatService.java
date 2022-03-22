package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.response.ChatDetailsDto;
import com.server.springboot.domain.dto.response.ChatDto;
import com.server.springboot.domain.dto.response.ChatMessageDto;
import com.server.springboot.domain.dto.response.ChatMessageNotificationDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {

    void addChat(RequestChatDto requestChatDto, MultipartFile imageFile);

    void editChatById(Long chatId, RequestChatDto requestChatDto, MultipartFile imageFile);

    List<ChatDto> findUserChats(Long userId);

    ChatDetailsDto findChatById(Long chatId);

    void deleteChatById(Long chatId);

    @Transactional
    ChatMessageNotificationDto saveMessageToChat(RequestChatMessageDto requestChatMessageDto);

    ChatMessageDto findChatMessageById(Long messageId);
}

