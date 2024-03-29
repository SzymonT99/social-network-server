package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.MessageType;
import com.server.springboot.domain.mapper.*;
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
    private final ChatDtoListMapper chatDtoListMapper;
    private final ChatDetailsDtoMapper chatDetailsDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final ChatMessageDtoMapper chatMessageDtoMapper;
    private final ChatDtoMapper chatDtoMapper;
    private final ImageDtoListMapper imageDtoListMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoleRepository roleRepository;

    @Autowired
    public ChatServiceImpl(JwtUtils jwtUtils, UserRepository userRepository, FileService fileService,
                           ChatRepository chatRepository, ChatMemberRepository chatMemberRepository,
                           ChatMessageRepository chatMessageRepository, ImageRepository imageRepository,
                           ChatDtoListMapper chatDtoListMapper, ChatDetailsDtoMapper chatDetailsDtoMapper,
                           UserDtoMapper userDtoMapper, ChatMessageDtoMapper chatMessageDtoMapper,
                           ChatDtoMapper chatDtoMapper, ImageDtoListMapper imageDtoListMapper,
                           NotificationService notificationService, SimpMessagingTemplate simpMessagingTemplate,
                           RoleRepository roleRepository) {
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
        this.roleRepository = roleRepository;
    }

    @Override
    public void createChat(RequestChatDto requestChatDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedInUserId();
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
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (!chat.getChatCreator().getUserId().equals(userId)
                && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
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
                    .sorted(Comparator.comparing(ChatMessage::getMessageId).reversed())
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
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            ChatMember chatMember = chatMemberRepository.findByUserMemberAndChat(loggedUser, chat)
                    .orElseThrow(() -> new NotFoundException("The logged user does not belong to chat with id: " + chatId));
            chatMember.setLastActivityDate(LocalDateTime.now());
            chatMemberRepository.save(chatMember);
        }

        return chatDetailsDtoMapper.convert(chat);
    }

    @Override
    public void deleteChatById(Long chatId) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (chat.getChatCreator() != loggedUser &&
                !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("No access to delete the chat");
        }

        chatRepository.delete(chat);
    }

    @Override
    public ChatMessageNotificationDto manageChatMessage(RequestChatMessageDto messageDto) {
        User senderUser = userRepository.findById(messageDto.getUserId()).get();
        Chat chat = chatRepository.findById(messageDto.getChatId())
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + messageDto.getChatId()));

        List<User> userChatMembersList = chat.getChatMembers().stream()
                .map(ChatMember::getUserMember)
                .collect(Collectors.toList());

        if (!chatMemberRepository.existsByChatAndUserMember(chat, senderUser)
                && messageDto.getMessageType() == MessageType.CHAT
                && !senderUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("No access to edit chat message");
        }

        ChatMessage chatMessage = null;

        if (messageDto.getMessageType() != MessageType.TYPING
                && messageDto.getMessageType() != MessageType.MESSAGE_EDIT
                && messageDto.getMessageType() != MessageType.MESSAGE_DELETE) {
            ChatMessage createdMessage = ChatMessage.builder()
                    .text(messageDto.getMessage())
                    .messageType(messageDto.getMessageType())
                    .image(null)
                    .createdAt(LocalDateTime.now())
                    .editedAt(null)
                    .isEdited(false)
                    .isDeleted(false)
                    .messageAuthor(senderUser)
                    .messageChat(chat)
                    .build();

            chatMessage = chatMessageRepository.save(createdMessage);

            for (User userMember : userChatMembersList) {
                notificationService.sendNotificationToUser(senderUser, userMember.getUserId(), ActionType.CHAT);
            }
        }

        UserDto messageAuthor = userDtoMapper.convert(senderUser);

        if (messageDto.getMessageType() != MessageType.MESSAGE_EDIT
                && messageDto.getMessageType() != MessageType.MESSAGE_DELETE) {
            return ChatMessageNotificationDto.builder()
                    .messageType(messageDto.getMessageType())
                    .typingMessage(messageDto.getMessage())
                    .chatId(messageDto.getChatId())
                    .messageId(chatMessage != null ? chatMessage.getMessageId() : null)
                    .author(messageAuthor)
                    .build();
        } else {
            return ChatMessageNotificationDto.builder()
                    .messageType(messageDto.getMessageType())
                    .chatId(messageDto.getChatId())
                    .messageId(messageDto.getEditedMessageId())
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

        if (!chatMemberRepository.existsByChatAndUserMember(chat, senderUser)
                && !senderUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("No access to add image to chat");
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
        Long loggedUserId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (!chatMemberRepository.existsByChatAndUserMember(chat, loggedUser)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("No access to fetch chat images");
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
    public void editChatMessageById(Long messageId, RequestChatMessageDto requestChatMessageDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Not found chat message with id: " + messageId));

        if ((chatMessage.getMessageAuthor() != loggedUser || chatMessage.getMessageType() != MessageType.CHAT)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Editing of messages is not allowed");
        }

        chatMessage.setText(requestChatMessageDto.getMessage());
        chatMessage.setEdited(true);
        chatMessage.setEditedAt(LocalDateTime.now());

        chatMessageRepository.save(chatMessage);
    }

    @Override
    public void deleteChatMessageById(Long messageId) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Not found chat message with id: " + messageId));

        if (chatMessage.getMessageAuthor() != loggedUser
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("No access to delete a chat message");
        }

        chatMessage.setDeleted(true);
        chatMessageRepository.save(chatMessage);
    }

    @Override
    public void addUserToChat(Long chatId, Long userId) {
        Long loggedUserId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            ChatMember loggedUserMember = chatMemberRepository.findByUserMemberAndChat(loggedUser, chat)
                    .orElseThrow(() -> new NotFoundException("The logged user does not belong to chat with id: " + chatId));

            if (!loggedUserMember.getCanAddOthers()) {
                throw new ForbiddenException("The logged user cannot add new users to the chat");
            }
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
        Long loggedUserId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Not found chat with id: " + chatId));

        if (chat.getChatCreator() != loggedUser
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
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
        Long loggedUserId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));

        User userFriend = userRepository.findById(userFriendId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userFriendId));

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

            return chatDtoMapper.convert(chatRepository.save(chat));
        } else {
            return chatDtoMapper.convert(chatWithFriend);
        }
    }
}