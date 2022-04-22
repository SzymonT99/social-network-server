package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.*;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.FriendRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.NotificationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private JwtUtils jwtUtils;
    @Spy
    private FriendInvitationDtoListMapper friendInvitationDtoListMapper;
    @Spy
    private SentFriendInvitationDtoListMapper sentFriendInvitationDtoListMapper;
    @Spy
    private FriendDtoListMapper friendDtoListMapper;
    @Spy
    private ProfilePhotoDtoMapper profilePhotoDtoMapper;
    @Spy
    private AddressDtoMapper addressDtoMapper;
    @Spy
    private UserDtoListMapper userDtoListMapper;

    @InjectMocks
    private FriendServiceImpl friendService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        friendInvitationDtoListMapper = new FriendInvitationDtoListMapper();
        sentFriendInvitationDtoListMapper = new SentFriendInvitationDtoListMapper();
        friendDtoListMapper = new FriendDtoListMapper();
        profilePhotoDtoMapper = new ProfilePhotoDtoMapper();
        addressDtoMapper = new AddressDtoMapper();
        userDtoListMapper = new UserDtoListMapper();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        user1 = User.builder()
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
                .friends(new HashSet<>())
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

        user2 = User.builder()
                .userId(2L)
                .username("Adam123")
                .password("Qwertyuiop")
                .email("adamKowalski@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .verifiedAccount(false)
                .activityStatus(ActivityStatus.OFFLINE)
                .isBlocked(false)
                .isBanned(false)
                .friends(new HashSet<>())
                .userProfile(UserProfile.builder()
                        .firstName("Adam")
                        .lastName("Kowalski")
                        .gender(Gender.MALE)
                        .dateOfBirth(LocalDate.parse("1980-01-05", formatter))
                        .age(LocalDate.now().getYear() - LocalDate.parse("1980-01-05", formatter).getYear())
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();
    }

    @Test
    public void shouldInviteToFriendsByUserId() {
        Long invitedUserId = 2L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(invitedUserId)).thenReturn(Optional.of(user2));
        when(friendRepository.existsByUserAndUserFriend(user1, user2)).thenReturn(false);

        doAnswer(invocation -> {
            Friend addedFriend = (Friend) invocation.getArgument(0);
            assertEquals(invitedUserId, addedFriend.getUserFriend().getUserId());
            return null;
        }).when(friendRepository).save(any(Friend.class));

        friendService.inviteToFriendsByUserId(invitedUserId);

        verify(friendRepository, times(1)).save(any(Friend.class));
    }

    @Test
    public void shouldFindAllUserReceivedInvitationsToFriends() {
        List<Friend> userFriends = new ArrayList<>();
        Friend friend = Friend.builder()
                .friendId(1L)
                .user(user2)
                .userFriend(user1)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .isInvitationAccepted(null)
                .build();
        userFriends.add(friend);
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(friendRepository.findByUserFriendAndIsInvitationAccepted(user1, null))
                .thenReturn(userFriends);

        List<FriendInvitationDto> resultFriendInvitations = friendService.findAllUserReceivedInvitationsToFriends(userId, false);

        assertNotNull(resultFriendInvitations);
        assertEquals(1, resultFriendInvitations.size());
        assertEquals(friendInvitationDtoListMapper.convert(userFriends), resultFriendInvitations);
    }

    @Test
    public void shouldAcceptFriendInvitation() {
        Friend friend = Friend.builder()
                .friendId(1L)
                .user(user2)
                .userFriend(user1)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .isInvitationAccepted(null)     // brak odpowiedzi na zaproszenie
                .build();

        Long inviterId = 2L;
        String reactionToInvitation = "accept";

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(user2));
        when(friendRepository.findByUserAndUserFriend(user2, user1))
                .thenReturn(Optional.of(friend));

        doAnswer(invocation -> {
            Friend addedFriend = (Friend) invocation.getArgument(0);
            assertNotNull(addedFriend.getFriendFromDate());
            assertTrue(addedFriend.getIsInvitationAccepted());
            return null;
        }).when(friendRepository).save(any(Friend.class));

        friendService.respondToFriendInvitation(inviterId, reactionToInvitation);

        verify(friendRepository, times(2)).save(any(Friend.class));
    }

    @Test
    public void shouldRejectFriendInvitation() {
        Friend friend = Friend.builder()
                .friendId(1L)
                .user(user2)
                .userFriend(user1)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .isInvitationAccepted(null)     // brak odpowiedzi na zaproszenie
                .build();

        Long inviterId = 2L;
        String reactionToInvitation = "reject";

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(user2));
        when(friendRepository.findByUserAndUserFriend(user2, user1))
                .thenReturn(Optional.of(friend));

        doAnswer(invocation -> {
            Friend rejectedFriend = (Friend) invocation.getArgument(0);
            assertFalse(rejectedFriend.getIsInvitationAccepted());
            return null;
        }).when(friendRepository).save(any(Friend.class));

        friendService.respondToFriendInvitation(inviterId, reactionToInvitation);

        verify(friendRepository, times(1)).save(any(Friend.class));
    }

    @Test
    public void shouldDeleteFriendById() {
        Long friendId = 1L;

        Friend userFriend = Friend.builder()
                .friendId(2L)
                .user(user2)
                .userFriend(user1)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .friendFromDate(LocalDateTime.now())
                .isInvitationAccepted(true)
                .build();

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(friendRepository.findById(friendId)).thenReturn(Optional.of(userFriend));

        friendService.deleteFriendById(friendId, false);

        verify(friendRepository, times(1)).deleteByUserAndUserFriend(user1, user2);
        verify(friendRepository, times(1)).deleteByUserAndUserFriend(user2, user1);
    }

    @Test
    public void shouldFindAllUserFriends() {
        List<Friend> friendList = new ArrayList<>();
        Friend friend = Friend.builder()
                .friendId(1L)
                .user(user1)
                .userFriend(user2)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .friendFromDate(LocalDateTime.now())
                .isInvitationAccepted(true)
                .build();
        friendList.add(friend);
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(friendRepository.findByUserAndIsInvitationAccepted(user1, true)).thenReturn(friendList);

        List<FriendDto> resultFriendList = friendService.findAllUserFriends(userId);

        assertNotNull(resultFriendList);
        assertEquals(1, resultFriendList.size());
        assertEquals(friendDtoListMapper.convert(friendList), resultFriendList);
    }

    @Test
    public void shouldFindAllUserSentInvitationsToFriends() {
        List<Friend> friendList = new ArrayList<>();
        Friend friend = Friend.builder()
                .friendId(1L)
                .user(user1)
                .userFriend(user2)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .isInvitationAccepted(null)     // nie zaakceptowano
                .build();
        friendList.add(friend);
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(friendRepository.findByUserAndIsInvitationAccepted(user1, null)).thenReturn(friendList);

        List<SentFriendInvitationDto> resultSentFriendInvitations = friendService.findAllUserSentInvitationsToFriends(userId);

        assertNotNull(resultSentFriendInvitations);
        assertEquals(1, resultSentFriendInvitations.size());
        assertEquals(sentFriendInvitationDtoListMapper.convert(friendList), resultSentFriendInvitations);
    }

    @Test
    public void shouldFindAllFriendSuggestions() {
        User user3 = new User(user2);
        user3.setUserId(3L);

        Friend friend1 = Friend.builder()
                .friendId(1L)
                .user(user1)
                .userFriend(user2)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .friendFromDate(LocalDateTime.now())
                .isInvitationAccepted(true)
                .build();

        Friend friend2 = Friend.builder()
                .friendId(2L)
                .user(user2)
                .userFriend(user3)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .friendFromDate(LocalDateTime.now())
                .isInvitationAccepted(true)
                .build();

        Friend friend3 = Friend.builder()
                .friendId(3L)
                .user(user3)
                .userFriend(user2)
                .invitationDate(LocalDateTime.now().minusDays(2L))
                .friendFromDate(LocalDateTime.now())
                .isInvitationAccepted(true)
                .build();

        user1.setFriends(new HashSet<Friend>() {{
            add(friend1);
        }});

        user2.setFriends(new HashSet<Friend>() {{
            add(friend2);
        }});

        user3.setFriends(new HashSet<Friend>() {{
            add(friend3);
        }});

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findAll()).thenReturn(users);
        when(friendRepository.existsByUserAndUserFriend(any(), any())).thenReturn(false);

        List<FriendSuggestionDto> friendSuggestionList = friendService.findAllFriendsSuggestions();

        assertEquals(1, friendSuggestionList.size());
        assertEquals(user3.getUserId(), friendSuggestionList.get(0).getUserId());
    }

}
