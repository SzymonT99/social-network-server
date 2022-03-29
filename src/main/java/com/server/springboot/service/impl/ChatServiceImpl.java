package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.MessageType;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.ChatService;
import com.server.springboot.service.FileService;
import com.server.springboot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final Converter<ChatDto, Chat> chatDtoMapper;
    private final Converter<List<ImageDto>, List<Image>> imageDtoListMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public ChatServiceImpl(JwtUtils jwtUtils, UserRepository userRepository, FileService fileService,
                           ChatRepository chatRepository, ChatMemberRepository chatMemberRepository,
                           ChatMessageRepository chatMessageRepository, ImageRepository imageRepository,
                           Converter<List<ChatDto>, List<Chat>> chatDtoListMapper,
                           Converter<ChatDetailsDto, Chat> chatDetailsDtoMapper,
                           Converter<UserDto, User> userDtoMapper, Converter<ChatMessageDto, ChatMessage> chatMessageDtoMapper,
                           Converter<ChatDto, Chat> chatDtoMapper, Converter<List<ImageDto>, List<Image>> imageDtoListMapper,
                           NotificationService notificationService, SimpMessagingTemplate simpMessagingTemplate) {
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
        this.chatDtoMapper = chatDtoMapper;
        this.imageDtoListMapper = imageDtoListMapper;
        this.notificationService = notificationService;
        this.simpMessagingTemplate = simpMessagingTemplate;
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

        ChatMember chatMemberCreator = ChatMember.builder()
                .chat(chat)
                .userMember(chatCreator)
                .addedIn(LocalDateTime.now())
                .canAddOthers(true)
                .hasMutedChat(false)
                .lastActivityDate(LocalDateTime.now())
                .build();

        ChatMessage initMessage = ChatMessage.builder()
                .messageType(MessageType.CREATE)
                .createdAt(LocalDateTime.now())
                .isEdited(false)
                .isDeleted(false)
                .messageAuthor(chatCreator)
                .messageChat(chat)
                .build();

        chatRepository.save(chat);
        chatMemberRepository.save(chatMemberCreator);
        chatMessageRepository.save(initMessage);

        if (requestChatDto.getAddedUsersId().size() != 0) {
            for (Long addedUserId : requestChatDto.getAddedUsersId()) {
                User addedUser = userRepository.findById(addedUserId)
                        .orElseThrow(() -> new NotFoundException("Not found user with id: " + addedUserId));

                ChatMember addedMember = ChatMember.builder()
                        .chat(chat)
                        .userMember(addedUser)
                        .addedIn(LocalDateTime.now())
                        .canAddOthers(false)
                        .hasMutedChat(false)
                        .lastActivityDate(LocalDateTime.now())
                        .build();

                ChatMessage chatMessage = ChatMessage.builder()
                        .messageType(MessageType.JOIN)
                        .createdAt(LocalDateTime.now())
                        .isEdited(false)
                        .isDeleted(false)
                        .messageAuthor(addedUser)
                        .messageChat(chat)
                        .build();

                chatMessageRepository.save(chatMessage);
                chatMemberRepository.save(addedMember);

                notificationService.sendNotificationToUser(chatCreator, addedUserId, ActionType.CHAT);
            }
        }
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

        List<ChatDto> userChatExtendDtoList = userChatDtoList.stream().map(chatDto -> {
            Chat currentChat = chatRepository.findById(chatDto.getChatId()).get();
            List<ChatMessage> chatMessages = currentChat.getChatMessages().stream()
                    .sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
                    .collect(Collectors.toList());
            ChatMember chatMember = chatMemberRepository.findByUserMemberAndChat(user, currentChat).get();

            List<User> messageAuthors = currentChat.getChatMembers().stream()
                    .map(ChatMember::getUserMember)
                    .filter(u -> u != user)
                    .collect(Collectors.toList());
            chatDto.setNewMessages(chatMessageRepository.countAllByMessageChatAndMessageAuthorInAndCreatedAtGreaterThan(
                    currentChat, messageAuthors, chatMember.getLastActivityDate()));

            if (chatMessages.size() > 0) {
                chatDto.setActivityDate(chatMessages.get(0).getCreatedAt().toString());
                chatDto.setLastMessage(chatMessages.get(0).getText());
                chatDto.setLastMessageAuthor(userDtoMapper.convert(chatMessages.get(0).getMessageAuthor()));
            }
            return chatDto;
        }).collect(Collectors.toList());

        return userChatExtendDtoList.stream()
                .sorted(Comparator.comparing(ChatDto::getNewMessages).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public ChatDetailsDto findChatById(Long chatId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        ChatMember chatMember = chatMemberRepository.findByUserMemberAndChat(loggedUser, chat)
                .orElseThrow(() -> new NotFoundException("The logged user does not belong to chat with id: " + chatId));

        chatMember.setLastActivityDate(LocalDateTime.now());

        chatMemberRepository.save(chatMember);

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
    public ChatMessageNotificationDto manageChatMessage(RequestChatMessageDto requestChatMessageDto) {
        User senderUser = userRepository.findById(requestChatMessageDto.getUserId())
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requestChatMessageDto.getUserId()));
        Chat chat = chatRepository.findById(requestChatMessageDto.getChatId())
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + requestChatMessageDto.getChatId()));

        List<User> userChatMembersList = chat.getChatMembers().stream()
                .map(ChatMember::getUserMember)
                .collect(Collectors.toList());

        if (!chatMemberRepository.existsByChatAndUserMember(chat, senderUser)
                && requestChatMessageDto.getMessageType() == MessageType.CHAT) {
            throw new ForbiddenException("User does not belong to the chat");
        }

        ChatMessage chatMessage = null;

        if (requestChatMessageDto.getMessageType() != MessageType.TYPING
                && requestChatMessageDto.getMessageType() != MessageType.MESSAGE_EDIT
                && requestChatMessageDto.getMessageType() != MessageType.MESSAGE_DELETE) {
            chatMessage = ChatMessage.builder()
                    .text(requestChatMessageDto.getMessage())
                    .messageType(requestChatMessageDto.getMessageType())
                    .image(null)
                    .createdAt(LocalDateTime.now())
                    .editedAt(null)
                    .isEdited(false)
                    .isDeleted(false)
                    .messageAuthor(senderUser)
                    .messageChat(chat)
                    .build();

            chatMessageRepository.save(chatMessage);

            for (User userMember : userChatMembersList) {
                notificationService.sendNotificationToUser(senderUser, userMember.getUserId(), ActionType.CHAT);
            }
        }

        UserDto messageAuthor = userDtoMapper.convert(senderUser);

        if (requestChatMessageDto.getMessageType() != MessageType.MESSAGE_EDIT
                && requestChatMessageDto.getMessageType() != MessageType.MESSAGE_DELETE) {
            return ChatMessageNotificationDto.builder()
                    .messageType(requestChatMessageDto.getMessageType())
                    .typingMessage(requestChatMessageDto.getMessage())
                    .chatId(requestChatMessageDto.getChatId())
                    .messageId(chatMessage != null ? chatMessage.getMessageId() : null)
                    .author(messageAuthor)
                    .build();
        } else {
            return ChatMessageNotificationDto.builder()
                    .messageType(requestChatMessageDto.getMessageType())
                    .chatId(requestChatMessageDto.getChatId())
                    .messageId(requestChatMessageDto.getEditedMessageId())
                    .author(messageAuthor)
                    .build();
        }
    }

    @Override
    public void saveChatImages(Long chatId, Long senderId, List<MultipartFile> imageFiles) {
        User senderUser = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + senderId));
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        List<User> userChatMembersList = chat.getChatMembers().stream()
                .map(ChatMember::getUserMember)
                .collect(Collectors.toList());

        if (!chatMemberRepository.existsByChatAndUserMember(chat, senderUser)) {
            throw new ForbiddenException("User does not belong to the chat");
        }

        Set<Image> images = fileService.storageImages(imageFiles, senderUser);

        for (Image image : images) {
            ChatMessage chatMessage = ChatMessage.builder()
                    .text("Wysłał(a) zdjęcie")
                    .messageType(MessageType.CHAT)
                    .image(image)
                    .createdAt(LocalDateTime.now())
                    .editedAt(null)
                    .isEdited(false)
                    .isDeleted(false)
                    .messageAuthor(senderUser)
                    .messageChat(chat)
                    .build();

            chatMessageRepository.save(chatMessage);

            ChatMessageNotificationDto chatMessageNotificationDto = ChatMessageNotificationDto.builder()
                    .messageType(MessageType.CHAT)
                    .chatId(chatId)
                    .messageId(chatMessage.getMessageId())
                    .author(userDtoMapper.convert(senderUser))
                    .build();

            simpMessagingTemplate.convertAndSend("/topic/messages/" + chatId, chatMessageNotificationDto);
        }

        for (User userMember : userChatMembersList) {
            notificationService.sendNotificationToUser(senderUser, userMember.getUserId(), ActionType.CHAT);
        }
    }

    @Override
    public List<ImageDto> findChatImages(Long chatId) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (!chatMemberRepository.existsByChatAndUserMember(chat, loggedUser)) {
            throw new ForbiddenException("User does not belong to the chat");
        }

        List<Image> chatImages = chat.getChatMessages().stream()
                .filter(chatMessage -> chatMessage.getImage() != null)
                .map(ChatMessage::getImage)
                .collect(Collectors.toList());
        return imageDtoListMapper.convert(chatImages);
    }

    @Override
    public void manageChatMemberNotifications(Long chatMemberId, boolean isChatMuted) {
        ChatMember chatMember = chatMemberRepository.findById(chatMemberId)
                .orElseThrow(() -> new NotFoundException("Not found chat member with id: " + chatMemberId));

        chatMember.setHasMutedChat(isChatMuted);
        chatMemberRepository.save(chatMember);
    }

    @Override
    public ChatMessageDto findChatMessageById(Long messageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Not found chat message with id: " + messageId));

        return chatMessageDtoMapper.convert(chatMessage);
    }

    @Override
    public void editChatMessageById(Long messageId, RequestChatMessageDto requestChatMessageDto, MultipartFile image) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Not found chat message with id: " + messageId));

        if (chatMessage.getMessageAuthor() != loggedUser || chatMessage.getMessageType() != MessageType.CHAT) {
            throw new ForbiddenException("Editing of messages is not allowed");
        }

        chatMessage.setText(requestChatMessageDto.getMessage());
        chatMessage.setEdited(true);
        chatMessage.setEditedAt(LocalDateTime.now());

        chatMessageRepository.save(chatMessage);
    }

    @Override
    public void deleteChatMessageById(Long messageId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Not found chat message with id: " + messageId));

        if (chatMessage.getMessageAuthor() != loggedUser) {
            throw new ForbiddenException("Only the author can delete a chat message");
        }

        chatMessage.setDeleted(true);
        chatMessageRepository.save(chatMessage);
    }

    @Override
    public void addUserToChat(Long chatId, Long userId) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        ChatMember loggedUserMember = chatMemberRepository.findByUserMemberAndChat(loggedUser, chat)
                .orElseThrow(() -> new NotFoundException("The logged user does not belong to chat with id: " + chatId));

        if (!loggedUserMember.getCanAddOthers()) {
            throw new ForbiddenException("The logged user cannot add new users to the chat");
        }

        User addedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        if (chatMemberRepository.existsByChatAndUserMember(chat, addedUser)) {
            throw new ConflictRequestException("The user has already been added to the chat");
        }

        ChatMember addedMember = ChatMember.builder()
                .chat(chat)
                .userMember(addedUser)
                .addedIn(LocalDateTime.now())
                .canAddOthers(false)
                .hasMutedChat(false)
                .lastActivityDate(LocalDateTime.now())
                .build();

        chatMemberRepository.save(addedMember);
    }

    @Override
    public void manageChatMemberPermission(Long chatId, Long chatMemberId, boolean canAddMembers) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (chat.getChatCreator() != loggedUser) {
            throw new ForbiddenException("The logged user cannot manage chat members permission");
        }

        ChatMember chatMember = chatMemberRepository.findById(chatMemberId)
                .orElseThrow(() -> new NotFoundException("Not found chat member with id: " + chatMemberId));

        chatMember.setCanAddOthers(canAddMembers);

        chatMemberRepository.save(chatMember);
    }

    @Override
    public void deleteChatMemberById(Long chatMemberId) {
        ChatMember chatMember = chatMemberRepository.findById(chatMemberId)
                .orElseThrow(() -> new NotFoundException("Not found chat member with id: " + chatMemberId));
        chatMemberRepository.delete(chatMember);
    }

    @Override
    public ChatDto getPrivateChatWithFriend(Long userFriendId) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));

        User userFriend = userRepository.findById(userFriendId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userFriendId));

        // Gdy czat prywatny nie istnieje
        List<ChatMember> chatMemberList = chatMemberRepository.findByUserMember(loggedUser);
        List<Chat> allUserPrivateChats = chatRepository.findByChatMembersInAndIsPrivate(chatMemberList, true);

        Chat chatWithFriend = null;

        for (Chat chat : allUserPrivateChats) {
            List<User> usersInChat = chat.getChatMembers().stream()
                    .map(ChatMember::getUserMember)
                    .collect(Collectors.toList());

            if (usersInChat.contains(userFriend)) {
                chatWithFriend = chat;
            }
        }

        if (chatWithFriend == null) {
            Chat chat = Chat.builder()
                    .name("PRIVATE")
                    .createdAt(LocalDateTime.now())
                    .isPrivate(true)
                    .isEdited(false)
                    .chatCreator(null)
                    .build();

            ChatMember chatMember1 = ChatMember.builder()
                    .chat(chat)
                    .userMember(loggedUser)
                    .addedIn(LocalDateTime.now())
                    .canAddOthers(null)
                    .hasMutedChat(false)
                    .lastActivityDate(LocalDateTime.now())
                    .build();

            ChatMember chatMember2 = ChatMember.builder()
                    .chat(chat)
                    .userMember(userFriend)
                    .addedIn(LocalDateTime.now())
                    .canAddOthers(null)
                    .hasMutedChat(false)
                    .lastActivityDate(LocalDateTime.now())
                    .build();

            Set<ChatMember> privateChatMembers = new HashSet<>();
            privateChatMembers.add(chatMember1);
            privateChatMembers.add(chatMember2);

            chat.setChatMembers(privateChatMembers);

            chatRepository.save(chat);

            return chatDtoMapper.convert(chat);
        } else {
            return chatDtoMapper.convert(chatWithFriend);
        }
    }
}