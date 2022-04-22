package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.*;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceImplTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileService fileService;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ChatMemberRepository chatMemberRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private RoleRepository roleRepository;
    @Spy
    private ChatDtoListMapper chatDtoListMapper;
    @Spy
    private ChatDetailsDtoMapper chatDetailsDtoMapper;
    @Spy
    private UserDtoMapper userDtoMapper;
    @Spy
    private ChatMessageDtoMapper chatMessageDtoMapper;
    @Spy
    private ChatDtoMapper chatDtoMapper;
    @Spy
    private ImageDtoListMapper imageDtoListMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private User user;
    private Chat chat;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        chatDtoListMapper = new ChatDtoListMapper();
        chatDetailsDtoMapper = new ChatDetailsDtoMapper();
        userDtoMapper = new UserDtoMapper();
        chatMessageDtoMapper = new ChatMessageDtoMapper();
        chatDtoMapper = new ChatDtoMapper();
        imageDtoListMapper = new ImageDtoListMapper();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        user = User.builder()
                .userId(1L)
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .verifiedAccount(false)
                .activityStatus(ActivityStatus.OFFLINE)
                .isBlocked(false)
                .isBanned(false)
                .memberOfGroups(new HashSet<>())
                .userProfile(UserProfile.builder()
                        .firstName("Jan")
                        .lastName("Nowak")
                        .gender(Gender.MALE)
                        .dateOfBirth(LocalDate.parse("1989-01-05", formatter))
                        .age(LocalDate.now().getYear() - LocalDate.parse("1989-01-05", formatter).getYear())
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();
        chat = Chat.builder()
                .chatId(1L)
                .name("Czat")
                .chatCreator(user)
                .createdAt(LocalDateTime.now())
                .chatMembers(new HashSet<ChatMember>() {{
                    add(ChatMember.builder()
                            .chatMemberId(1L)
                            .chat(chat)
                            .userMember(user)
                            .addedIn(LocalDateTime.now().minusDays(2L))
                            .canAddOthers(true)
                            .lastActivityDate(LocalDateTime.now())
                            .build());
                }})
                .chatMessages(new HashSet<>())
                .build();

        chatMessage = ChatMessage.builder()
                .messageId(1L)
                .messageType(MessageType.CHAT)
                .text("Nowa wiadomość czatu")
                .createdAt(LocalDateTime.now())
                .isEdited(false)
                .isDeleted(false)
                .messageAuthor(user)
                .messageChat(chat)
                .build();
    }

    @Test
    public void shouldCreateChat() {
        RequestChatDto requestChatDto = RequestChatDto.builder()
                .name("Czat")
                .addedUsersId(new ArrayList<>())
                .isPrivate(false)
                .build();
        MockMultipartFile chatImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileService.storageOneImage(chatImage, user, false)).thenReturn(new Image());

        doAnswer(invocation -> {
            Chat createdChat = (Chat) invocation.getArgument(0);
            assertNotNull(createdChat.getImage());
            assertEquals(requestChatDto.getName(), createdChat.getName());
            return null;
        }).when(chatRepository).save(any(Chat.class));

        doAnswer(invocation -> {
            ChatMessage initMessage = (ChatMessage) invocation.getArgument(0);
            assertEquals(MessageType.CREATE, initMessage.getMessageType());
            return null;
        }).when(chatMessageRepository).save(any(ChatMessage.class));

        chatService.createChat(requestChatDto, chatImage);

        verify(chatRepository, times(1)).save(any(Chat.class));
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(chatMemberRepository, times(1)).save(any(ChatMember.class));
    }

    @Test
    public void shouldEditChatById() {
        Long chatId = 1L;
        RequestChatDto requestChatDto = RequestChatDto.builder()
                .name("Czat edytowany") // zmiana
                .addedUsersId(new ArrayList<>())
                .isPrivate(false)
                .build();
        MockMultipartFile chatImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(fileService.storageOneImage(chatImage, user, false)).thenReturn(new Image());

        doAnswer(invocation -> {
            Chat updatedChat = (Chat) invocation.getArgument(0);
            assertEquals(chatId, updatedChat.getChatId());
            assertEquals(requestChatDto.getName(), updatedChat.getName());
            return null;
        }).when(chatRepository).save(any(Chat.class));

        chatService.editChatById(chatId, requestChatDto, chatImage);

        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    public void shouldFindUserChats() {
        Chat chat2 = Chat.builder()
                .chatId(2L)
                .name("Czat 2")
                .chatCreator(user)
                .createdAt(LocalDateTime.now())
                .chatMembers(new HashSet<>())
                .chatMessages(new HashSet<>())
                .build();

        List<ChatMember> memberOfChats = new ArrayList<>();
        ChatMember chatMember1 = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        ChatMember chatMember2 = ChatMember.builder()
                .chatMemberId(2L)
                .chat(chat2)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        chat.setChatMembers(new HashSet<ChatMember>() {{
            add(chatMember1);
        }});
        chat2.setChatMembers(new HashSet<ChatMember>() {{
            add(chatMember2);
        }});
        memberOfChats.add(chatMember1);
        memberOfChats.add(chatMember2);

        Long userId = 1L;

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatMemberRepository.findByUserMember(user)).thenReturn(memberOfChats);

        when(chatRepository.findById(chat.getChatId())).thenReturn(Optional.of(chat));
        when(chatMemberRepository.findByUserMemberAndChat(user, chat)).thenReturn(Optional.of(chatMember1));
        when(chatRepository.findById(chat2.getChatId())).thenReturn(Optional.of(chat2));
        when(chatMemberRepository.findByUserMemberAndChat(user, chat2)).thenReturn(Optional.of(chatMember2));

        List<ChatDto> resultUserChats = chatService.findUserChats(userId);

        assertEquals(2, resultUserChats.size());
        assertEquals(userId, resultUserChats.get(0).getMembers().get(0).getUser().getUserId());
    }

    @Test
    public void shouldFindChatById() {
        ChatMember chatMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        Long chatId = 1L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(chatMemberRepository.findByUserMemberAndChat(user, chat)).thenReturn(Optional.of(chatMember));

        ChatDetailsDto resultChat = chatService.findChatById(chatId);

        assertNotNull(resultChat);
        assertEquals(chatId, resultChat.getChatId());
        assertEquals(chat.getName(), resultChat.getName());
    }

    @Test
    public void shouldDeleteChatById() {
        Long chatId = 1L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        chatService.deleteChatById(chatId);

        verify(chatRepository, times(1)).delete(chat);
    }

    @Test
    public void shouldThrowErrorWhenDeleteChatByIdAndUserIsNotCreator() {
        User creator = new User(user);
        user.setUserId(2L);
        chat.setChatCreator(creator);

        Long chatId = 1L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        assertThatExceptionOfType(ForbiddenException.class)
                .isThrownBy(() -> {
                    chatService.deleteChatById(chatId);
                }).withMessage("No access to delete the chat");

        verify(chatRepository, never()).delete(chat);
    }

    @Test
    public void shouldManageChatMessage() {
        ChatMember chatMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        user.setMemberOfChats(new HashSet<ChatMember>() {{
            add(chatMember);
        }});
        RequestChatMessageDto requestChatMessageDto = RequestChatMessageDto.builder()
                .chatId(1L)
                .userId(1L)
                .messageType(MessageType.CHAT)
                .message("Nowa wiadomość czatu")
                .build();

        when(userRepository.findById(requestChatMessageDto.getUserId())).thenReturn(Optional.of(user));
        when(chatRepository.findById(requestChatMessageDto.getChatId())).thenReturn(Optional.of(chat));
        when(chatMemberRepository.existsByChatAndUserMember(chat, user)).thenReturn(true);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        ChatMessageNotificationDto resultNotification = chatService.manageChatMessage(requestChatMessageDto);

        assertNotNull(resultNotification);
        assertNotNull(resultNotification.getMessageId());
        assertEquals(requestChatMessageDto.getChatId(), resultNotification.getChatId());
    }

    @Test
    public void shouldSaveChatImages() {
        Long chatId = 1L;
        Long senderId = 1L;
        List<MultipartFile> imageFiles = new ArrayList<>();
        MockMultipartFile file1 = new MockMultipartFile("image1", new byte[1]);
        MockMultipartFile file2 = new MockMultipartFile("image2", new byte[1]);
        imageFiles.add(file1);
        imageFiles.add(file2);

        List<Image> sentImages = new ArrayList<>();
        Image image1 = Image.builder()
                .imageId(UUID.randomUUID().toString())
                .userProfile(user.getUserProfile())
                .addedIn(LocalDateTime.now())
                .filePath("/uploads/image1")
                .filename("image1.png")
                .build();
        Image image2 = Image.builder()
                .imageId(UUID.randomUUID().toString())
                .userProfile(user.getUserProfile())
                .addedIn(LocalDateTime.now().minusHours(1L))
                .filePath("/uploads/image2")
                .filename("image2.png")
                .build();
        sentImages.add(image1);
        sentImages.add(image2);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(chatMemberRepository.existsByChatAndUserMember(chat, user)).thenReturn(true);

        when(fileService.storageImages(imageFiles, user)).thenReturn(new HashSet<>(sentImages));

        doAnswer(invocation -> {
            ChatMessage createdMessage = (ChatMessage) invocation.getArgument(0);
            assertNotNull(createdMessage.getImage());
            assertEquals("Wysłał(a) zdjęcie", createdMessage.getText());
            assertEquals(chat, createdMessage.getMessageChat());
            return null;
        }).when(chatMessageRepository).save(any(ChatMessage.class));

        chatService.saveChatImages(chatId, senderId, imageFiles);

        verify(simpMessagingTemplate, times(2))
                .convertAndSend(eq("/topic/messages/" + chatId), any(ChatMessageNotificationDto.class));
    }

    @Test
    public void shouldManageChatMuted() {
        ChatMember chatMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();

        Long chatMemberId = 1L;
        boolean isChatMuted = true;

        when(chatMemberRepository.findById(chatMemberId)).thenReturn(Optional.of(chatMember));

        doAnswer(invocation -> {
            ChatMember updatedChatMember = (ChatMember) invocation.getArgument(0);
            assertEquals(isChatMuted, updatedChatMember.isHasMutedChat());
            return null;
        }).when(chatMemberRepository).save(any(ChatMember.class));

        chatService.manageChatMemberNotifications(chatMemberId, isChatMuted);

        verify(chatMemberRepository, times(1)).save(chatMember);
    }

    @Test
    public void shouldFindChatMessageById() {
        Long messageId = 1L;

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(chatMessage));

        ChatMessageDto resultMessage = chatService.findChatMessageById(messageId);

        assertNotNull(resultMessage);
        assertEquals(chatMessageDtoMapper.convert(chatMessage), resultMessage);
    }

    @Test
    public void shouldEditChatMessageById() {
        Long messageId = 1L;
        RequestChatMessageDto requestChatMessageDto = RequestChatMessageDto.builder()
                .chatId(1L)
                .userId(1L)
                .messageType(MessageType.CHAT)
                .message("Wiadomość edytowana")     // zmiana
                .build();

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(chatMessage));

        doAnswer(invocation -> {
            ChatMessage updatedMessage = (ChatMessage) invocation.getArgument(0);
            assertEquals(messageId, updatedMessage.getMessageId());
            assertEquals(requestChatMessageDto.getMessage(), updatedMessage.getText());
            return null;
        }).when(chatMessageRepository).save(any(ChatMessage.class));

        chatService.editChatMessageById(messageId, requestChatMessageDto);

        verify(chatMessageRepository, times(1)).save(chatMessage);
    }

    @Test
    public void shouldDeleteChatMessageById() {
        Long messageId = 1L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(chatMessage));

        doAnswer(invocation -> {
            ChatMessage deletedMessage = (ChatMessage) invocation.getArgument(0);
            assertEquals(messageId, deletedMessage.getMessageId());
            assertTrue(deletedMessage.isDeleted());
            return null;
        }).when(chatMessageRepository).save(any(ChatMessage.class));

        chatService.deleteChatMessageById(messageId);

        verify(chatMessageRepository, times(1)).save(chatMessage);
    }

    @Test
    public void shouldAddUserToChat() {
        ChatMember loggedInMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        chat.setChatMembers(new HashSet<ChatMember>() {{
            add(loggedInMember);
        }});

        User addedUser = new User(user);
        addedUser.setUserId(2L);
        Long chatId = 1L;
        Long addedUserId = 2L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(chatMemberRepository.findByUserMemberAndChat(user, chat)).thenReturn(Optional.of(loggedInMember));

        when(userRepository.findById(addedUserId)).thenReturn(Optional.of(addedUser));
        when(chatMemberRepository.existsByChatAndUserMember(chat, addedUser)).thenReturn(false);

        doAnswer(invocation -> {
            ChatMember addedMember = (ChatMember) invocation.getArgument(0);
            assertEquals(addedUserId, addedMember.getUserMember().getUserId());
            assertEquals(addedUser, addedMember.getUserMember());
            assertEquals(chatId, addedMember.getChat().getChatId());
            return null;
        }).when(chatMemberRepository).save(any(ChatMember.class));

        chatService.addUserToChat(chatId, addedUserId);

        verify(chatMemberRepository, times(1)).save(any(ChatMember.class));
    }

    @Test
    public void shouldThrowErrorWhenAddUserToChatAndLoggedInUserHasNotPermission() {
        ChatMember loggedInMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(false)     // nie może dodawać użytkowników
                .lastActivityDate(LocalDateTime.now())
                .build();
        chat.setChatMembers(new HashSet<ChatMember>() {{
            add(loggedInMember);
        }});

        User addedUser = new User(user);
        addedUser.setUserId(2L);
        Long chatId = 1L;
        Long addedUserId = 2L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(chatMemberRepository.findByUserMemberAndChat(user, chat)).thenReturn(Optional.of(loggedInMember));

        assertThatExceptionOfType(ForbiddenException.class)
                .isThrownBy(() -> {
                    chatService.addUserToChat(chatId, addedUserId);
                }).withMessage("The logged user cannot add new users to the chat");

        verify(chatMemberRepository, never()).save(any(ChatMember.class));
    }

    @Test
    public void shouldManageChatMemberPermission() {
        ChatMember loggedInMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        chat.setChatMembers(new HashSet<ChatMember>() {{
            add(loggedInMember);
        }});

        User user2 = new User(user);
        user2.setUserId(2L);
        ChatMember anotherMember = ChatMember.builder()
                .chatMemberId(2L)
                .chat(chat)
                .userMember(user2)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(false)
                .lastActivityDate(LocalDateTime.now())
                .build();

        Long chatId = 1L;
        Long chatMemberId = 2L;
        boolean canAddMembers = true;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        when(chatMemberRepository.findById(chatMemberId)).thenReturn(Optional.of(anotherMember));

        doAnswer(invocation -> {
            ChatMember updatedMember = (ChatMember) invocation.getArgument(0);
            assertEquals(chatMemberId, updatedMember.getChatMemberId());
            assertEquals(canAddMembers, updatedMember.getCanAddOthers());
            return null;
        }).when(chatMemberRepository).save(any(ChatMember.class));

        chatService.manageChatMemberPermission(chatId, chatMemberId, canAddMembers);

        verify(chatMemberRepository, times(1)).save(any(ChatMember.class));
    }

    @Test
    public void shouldDeleteChatMemberById() {
        ChatMember chatMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();

        Long chatMemberId = 1L;

        when(chatMemberRepository.findById(chatMemberId)).thenReturn(Optional.of(chatMember));

        chatService.deleteChatMemberById(chatMemberId);

        verify(chatMemberRepository, times(1)).delete(chatMember);
    }

    @Test
    public void shouldInitPrivateChatWithFriend() {
        ChatMemberDtoListMapper chatMemberDtoListMapper = new ChatMemberDtoListMapper();
        List<ChatMember> memberOfChats = new ArrayList<>();
        ChatMember loggedInMember = ChatMember.builder()
                .chatMemberId(1L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        memberOfChats.add(loggedInMember);

        User userFriend = new User(user);
        userFriend.setUserId(2L);
        Long userFriendId = 2L;

        List<ChatMember> privateChatMembers = new ArrayList<>();
        Chat privateChat = Chat.builder()
                .chatId(2L)
                .name("Czat prywatny")
                .chatCreator(null)
                .createdAt(LocalDateTime.now())
                .chatMembers(new HashSet<>())
                .chatMessages(new HashSet<>())
                .build();

        ChatMember privateMember1 = ChatMember.builder()
                .chatMemberId(2L)
                .chat(chat)
                .userMember(user)
                .addedIn(LocalDateTime.now())
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        ChatMember privateMember2 = ChatMember.builder()
                .chatMemberId(3L)
                .chat(chat)
                .userMember(userFriend)
                .addedIn(LocalDateTime.now())
                .canAddOthers(true)
                .lastActivityDate(LocalDateTime.now())
                .build();
        privateChatMembers.add(privateMember1);
        privateChatMembers.add(privateMember2);
        privateChat.setChatMembers(new HashSet<>(privateChatMembers));

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(userFriendId)).thenReturn(Optional.of(userFriend));

        when(chatMemberRepository.findByUserMember(user)).thenReturn(memberOfChats);
        when(chatRepository.findByChatMembersInAndIsPrivate(memberOfChats, true)).thenReturn(new ArrayList<>());
        when(chatRepository.save(any(Chat.class))).thenReturn(privateChat);

        ChatDto resultChat =  chatService.getPrivateChatWithFriend(userFriendId);

        assertNotNull(resultChat);
        assertEquals(2, resultChat.getMembers().size());
        assertEquals(chatMemberDtoListMapper.convert(privateChatMembers), resultChat.getMembers());
    }

}
