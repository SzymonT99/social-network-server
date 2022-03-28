package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.response.*;
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
    ChatMessageNotificationDto manageChatMessage(RequestChatMessageDto requestChatMessageDto);

    ChatMessageDto findChatMessageById(Long messageId);

    void editChatMessageById(Long messageId, RequestChatMessageDto requestChatMessageDto, MultipartFile image);

    void deleteChatMessageById(Long messageId);

    void addUserToChat(Long chatId, Long userId);

    void manageChatMemberPermission(Long chatId, Long chatMemberId, boolean canAddMembers);

    void deleteChatMemberById(Long chatMemberId);

    ChatDto getPrivateChatWithFriend(Long userFriendId);

    void saveChatImages(Long chatId, Long senderId, List<MultipartFile> imageFiles);

    List<ImageDto> findChatImages(Long chatId);

    void manageChatMemberNotifications(Long chatMemberId, boolean isChatMuted);
}

