package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.ChatService;
import com.server.springboot.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ImageRepository imageRepository;
    private final Converter<List<ChatDto>, List<Chat>> chatDtoListMapper;
    private final Converter<ChatDetailsDto, Chat> chatDetailsDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<ChatMessageDto, ChatMessage> chatMessageDtoMapper;

    @Autowired
    public ChatServiceImpl(JwtUtils jwtUtils, UserRepository userRepository, FileService fileService,
                           ChatRepository chatRepository, ChatMemberRepository chatMemberRepository,
                           ChatMessageRepository chatMessageRepository, ImageRepository imageRepository,
                           Converter<List<ChatDto>, List<Chat>> chatDtoListMapper,
                           Converter<ChatDetailsDto, Chat> chatDetailsDtoMapper,
                           Converter<UserDto, User> userDtoMapper, Converter<ChatMessageDto, ChatMessage> chatMessageDtoMapper) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.imageRepository = imageRepository;
        this.chatDtoListMapper = chatDtoListMapper;
        this.chatDetailsDtoMapper = chatDetailsDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.chatMessageDtoMapper = chatMessageDtoMapper;
    }

    @Override
    public void addChat(RequestChatDto requestChatDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedUserId();
        User chatCreator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Chat chat = Chat.builder()
                .name(requestChatDto.getName())
                .createdAt(LocalDateTime.now())
                .isPrivate(false)
                .isEdited(false)
                .chatCreator(chatCreator)
                .build();

        if (imageFile != null) {
            Image image = fileService.storageOneImage(imageFile, chatCreator, false);
            chat.setImage(image);
        }

        ChatMember chatMember = ChatMember.builder()
                .chat(chat)
                .userMember(chatCreator)
                .addedIn(LocalDateTime.now())
                .canAddOthers(true)
                .hasMutedChat(false)
                .lastActivityDate(LocalDateTime.now())
                .build();

        chatRepository.save(chat);
        chatMemberRepository.save(chatMember);
    }

    @Override
    public void editChatById(Long chatId, RequestChatDto requestChatDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedUserId();
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (!chat.getChatCreator().getUserId().equals(userId)) {
            throw new ForbiddenException("Invalid chat creator id - chat editing access forbidden");
        }

        if (chat.getImage() != null) {
            String lastImageId = chat.getImage().getImageId();
            chat.setImage(null);
            imageRepository.deleteByImageId(lastImageId);
        }

        if (imageFile != null) {
            Image updatedImages = fileService.storageOneImage(imageFile, chat.getChatCreator(), false);
            chat.setImage(updatedImages);
        }

        chat.setName(requestChatDto.getName());
        chat.setEdited(true);

        chatRepository.save(chat);
    }

    @Override
    public List<ChatDto> findUserChats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        List<Chat> userChats = chatMemberRepository.findByUserMember(user).stream()
                .map(ChatMember::getChat)
                .collect(Collectors.toList());

        List<ChatDto> userChatDtoList = chatDtoListMapper.convert(userChats);

        return userChatDtoList.stream().map(chatDto -> {
            Chat currentChat = chatRepository.findById(chatDto.getChatId()).get();
            ChatMember chatMember = chatMemberRepository.findByUserMemberAndChat(user, currentChat).get();
            chatDto.setNewMessages(chatMessageRepository.countAllByMessageChatAndCreatedAtGreaterThan(currentChat, chatMember.getLastActivityDate()));
            return chatDto;
        }).collect(Collectors.toList());
    }

    @Override
    public ChatDetailsDto findChatById(Long chatId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (!chatMemberRepository.existsByChatAndUserMember(chat, loggedUser)) {
            throw new ForbiddenException("User does not belong to the chat");
        }

        return chatDetailsDtoMapper.convert(chat);
    }

    @Override
    public void deleteChatById(Long chatId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (chat.getChatCreator() != loggedUser) {
            throw new ForbiddenException("Only the author can delete the chat");
        }

        chatRepository.delete(chat);
    }

    @Override
    public ChatMessageNotificationDto saveMessageToChat(RequestChatMessageDto requestChatMessageDto) {
        User senderUser = userRepository.findById(requestChatMessageDto.getUserId())
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requestChatMessageDto.getUserId()));
        Chat chat = chatRepository.findById(requestChatMessageDto.getChatId())
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + requestChatMessageDto.getChatId()));

        if (!chatMemberRepository.existsByChatAndUserMember(chat, senderUser)) {
            throw new ForbiddenException("User does not belong to the chat");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .text(requestChatMessageDto.getMessage())
                .image(null)
                .createdAt(LocalDateTime.now())
                .editedAt(null)
                .isEdited(false)
                .isDeleted(false)
                .messageAuthor(senderUser)
                .messageChat(chat)
                .build();

        chatMessageRepository.save(chatMessage);

        UserDto messageAuthor = userDtoMapper.convert(chatMessage.getMessageAuthor());

        return ChatMessageNotificationDto.builder()
                .messageType(requestChatMessageDto.getMessageType())
                .chatId(requestChatMessageDto.getChatId())
                .messageId(chatMessage.getMessageId())
                .author(messageAuthor)
                .build();
    }

    @Override
    public ChatMessageDto findChatMessageById(Long messageId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Not found chat message with id: " + messageId));

        if (!chatMemberRepository.existsByChatAndUserMember(chatMessage.getMessageChat(), loggedUser)) {
            throw new ForbiddenException("User does not belong to the chat with this message");
        }

        return chatMessageDtoMapper.convert(chatMessage);
    }
}