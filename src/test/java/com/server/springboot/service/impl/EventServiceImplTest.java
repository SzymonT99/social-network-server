package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestAddressDto;
import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.EventInvitationDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.key.UserEventKey;
import com.server.springboot.domain.enumeration.*;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ConflictRequestException;
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
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private FileService fileService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventMemberRepository eventMemberRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private SharedEventRepository sharedEventRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Spy
    private EventMapper eventMapper;
    @Spy
    private AddressMapper addressMapper;
    @Spy
    private EventDtoMapper eventDtoMapper;
    @Spy
    private EventDtoListMapper eventDtoListMapper;
    @Spy
    private EventInvitationDtoListMapper eventInvitationDtoListMapper;
    @Spy
    private SharedEventDtoListMapper sharedEventDtoListMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {

        eventMapper = new EventMapper();
        addressMapper = new AddressMapper();
        eventDtoMapper = new EventDtoMapper();
        eventDtoListMapper = new EventDtoListMapper();
        eventInvitationDtoListMapper = new EventInvitationDtoListMapper();
        sharedEventDtoListMapper = new SharedEventDtoListMapper();

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

        event = Event.builder()
                .eventId(1L)
                .title("Wydarzenie")
                .description("Opis")
                .eventCreator(user)
                .createdAt(LocalDateTime.now())
                .eventDate(LocalDateTime.now())
                .members(new HashSet<>())
                .sharing(new HashSet<>())
                .eventAddress(Address.builder()
                        .addressId(1L)
                        .country("Polska")
                        .city("Tarnów")
                        .street("ul. Narutowicza")
                        .zipCode("33-100")
                        .build())
                .isDeleted(false)
                .build();
    }

    @Test
    public void shouldCreateEvent() {
        RequestEventDto requestEventDto = RequestEventDto.builder()
                .title("Wydarzenie")
                .description("Opis")
                .eventDate("2022-05-20 20:00")
                .eventAddress(RequestAddressDto.builder()
                        .country("Polska")
                        .city("Tarnów")
                        .street("ul. Narutowicza")
                        .zipCode("33-100")
                        .build())
                .build();
        MockMultipartFile eventImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileService.storageOneImage(eventImage, user, false)).thenReturn(new Image());

        EventDto createdEvent = eventService.createEvent(requestEventDto, eventImage);
        assertNotNull(createdEvent);
        assertNotNull(createdEvent.getImage());
        assertEquals(requestEventDto.getTitle(), createdEvent.getTitle());
        assertEquals(requestEventDto.getDescription(), createdEvent.getDescription());

        verify(eventRepository, times(1)).save(any(Event.class));
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    public void shouldEditEvent() throws IOException {
        Long eventId = 1L;
        RequestEventDto requestEventDto = RequestEventDto.builder()
                .title("Wydarzenie edytowane")  // zmiana
                .description("Opis")
                .eventDate("2022-05-20 20:00")
                .eventAddress(RequestAddressDto.builder()
                        .country("Polska")
                        .city("Tarnów")
                        .street("ul. Narutowicza")
                        .zipCode("33-100")
                        .build())
                .build();
        MockMultipartFile eventImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(fileService.storageOneImage(eventImage, user, false)).thenReturn(new Image());

        doAnswer(invocation -> {
            Event updatedEvent = (Event) invocation.getArgument(0);
            assertEquals(requestEventDto.getTitle(), updatedEvent.getTitle());
            return null;
        }).when(eventRepository).save(any(Event.class));

        eventService.editEvent(eventId, requestEventDto, eventImage);

        verify(eventRepository, times(1)).save(event);
    }

    @Test
    public void shouldThrowErrorWhenEditEventAndUserIsNotAuthor() throws IOException {
        User user2 = new User(user);
        user2.setUserId(2L);
        event.setEventCreator(user2);

        Long eventId = 1L;
        RequestEventDto requestEventDto = RequestEventDto.builder()
                .title("Wydarzenie edytowane")  // zmiana
                .description("Opis")
                .eventDate("2022-05-20 20:00")
                .eventAddress(RequestAddressDto.builder()
                        .country("Polska")
                        .city("Tarnów")
                        .street("ul. Narutowicza")
                        .zipCode("33-100")
                        .build())
                .build();
        MockMultipartFile eventImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        assertThatExceptionOfType(ForbiddenException.class)
                .isThrownBy(() -> {
                    eventService.editEvent(eventId, requestEventDto, eventImage);
                }).withMessage("Event editing access forbidden");

        verify(eventRepository, never()).save(event);
    }

    @Test
    public void shouldDeleteEventById() throws IOException {
        Long eventId = 1L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        eventService.deleteEventById(eventId, false);

        verify(eventRepository, times(1)).deleteByEventId(eventId);
    }

    @Test
    public void shouldFindAllEvents() {
        List<Event> events = new ArrayList<>();
        events.add(event);

        when(eventRepository.findByIsDeletedOrderByCreatedAtDesc(false)).thenReturn(events);

        List<EventDto> resultEvents = eventService.findAllEvents();

        assertEquals(1, resultEvents.size());
        assertEquals(eventDtoListMapper.convert(events), resultEvents);
    }

    @Test
    public void shouldInviteUserToEvents() {
        User user2 = new User(user);
        user2.setUserId(2L);

        Long eventId = 1L;
        Long invitedUserId = 2L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(invitedUserId)).thenReturn(Optional.of(user2));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMemberRepository.existsByEventMemberAndEvent(user2, event)).thenReturn(false);

        doAnswer(invocation -> {
            EventMember eventMember = (EventMember) invocation.getArgument(0);
            assertEquals(invitedUserId, eventMember.getEventMember().getUserId());
            return null;
        }).when(eventMemberRepository).save(any(EventMember.class));

        eventService.inviteUser(eventId, invitedUserId);

        verify(eventMemberRepository, times(1)).save(any(EventMember.class));
    }

    @Test
    public void shouldThrowErrorWhenInviteUserToEvent() {
        User user2 = new User(user);
        user2.setUserId(2L);

        Long eventId = 1L;
        Long invitedUserId = 2L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(invitedUserId)).thenReturn(Optional.of(user2));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMemberRepository.existsByEventMemberAndEvent(user2, event)).thenReturn(true);

        assertThatExceptionOfType(ConflictRequestException.class)
                .isThrownBy(() -> {
                    eventService.inviteUser(eventId, invitedUserId);
                }).withMessage("The user has already reacted to the event or an invitation has already been sent to him");

        verify(eventMemberRepository, never()).save(any(EventMember.class));
    }

    @Test
    public void shouldFindAllUserEventInvitation() {
        User user2 = new User(user);
        user2.setUserId(2L);

        List<EventMember> memberOfEvents = new ArrayList<>();
        EventMember eventMember = EventMember.builder()
                .eventMemberId(1L)
                .eventMember(user2)
                .event(event)
                .invitationDate(LocalDateTime.now().minusDays(1L))
                .participationStatus(EventParticipationStatus.INVITED)
                .build();
        memberOfEvents.add(eventMember);

        when(jwtUtils.getLoggedInUserId()).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(eventMemberRepository.findByEventMemberAndParticipationStatus(user2, EventParticipationStatus.INVITED))
                .thenReturn(memberOfEvents);

        List<EventInvitationDto> eventInvitationList = eventService.findAllUserEventInvitation(false);

        assertEquals(1, eventInvitationList.size());
    }

    @Test
    public void shouldRespondToEventWithTakePartDecision() {
        Long eventId = 1L;
        String reactionToEvent = EventParticipationStatus.TAKE_PART.toString();

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMemberRepository.existsByEventMemberAndEvent(user, event)).thenReturn(false);

        doAnswer(invocation -> {
            EventMember eventMember = (EventMember) invocation.getArgument(0);
            assertEquals(reactionToEvent, eventMember.getParticipationStatus().toString());
            return null;
        }).when(eventMemberRepository).save(any(EventMember.class));

        eventService.respondToEvent(eventId, reactionToEvent);

        verify(eventMemberRepository, times(1)).save(any(EventMember.class));
    }

    @Test
    public void shouldShareEvent() {
        Long eventId = 1L;

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(sharedEventRepository.existsBySharedEventUserAndEvent(user, event)).thenReturn(false);

        doAnswer(invocation -> {
            SharedEvent sharedEvent = (SharedEvent) invocation.getArgument(0);
            assertEquals(eventId, sharedEvent.getEvent().getEventId());
            return null;
        }).when(sharedEventRepository).save(any(SharedEvent.class));

        eventService.shareEvent(eventId);

        verify(sharedEventRepository, times(1)).save(any(SharedEvent.class));
    }

    @Test
    public void shouldDeleteSharedEvent() {
        Long eventId = 1L;

        SharedEvent sharedEvent = SharedEvent.builder()
                .id(UserEventKey.builder().userId(1L).eventId(1L).build())
                .sharedEventUser(user)
                .event(event)
                .date(LocalDateTime.now())
                .build();

        when(jwtUtils.getLoggedInUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(sharedEventRepository.existsBySharedEventUserAndEvent(user, event)).thenReturn(true);
        when(sharedEventRepository.findBySharedEventUserAndEvent(user, event)).thenReturn(Optional.of(sharedEvent));

        eventService.deleteSharedEvent(eventId);

        verify(sharedEventRepository, times(1)).delete(sharedEvent);
    }

    @Test
    public void shouldFindAllSharedEventsByUser() {
        List<SharedEvent> sharedEvents = new ArrayList<>();
        SharedEvent sharedEvent = SharedEvent.builder()
                .id(UserEventKey.builder().userId(1L).eventId(1L).build())
                .sharedEventUser(user)
                .event(event)
                .date(LocalDateTime.now())
                .build();
        sharedEvents.add(sharedEvent);
        user.setSharedEvents(new HashSet<SharedEvent>() {{
            add(sharedEvent);
        }});

        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sharedEventRepository.findBySharedEventUser(user)).thenReturn(sharedEvents);

        List<SharedEventDto> resultSharedEvents = eventService.findAllSharedEventsByUser(userId);

        assertEquals(1, resultSharedEvents.size());
        assertEquals(sharedEventDtoListMapper.convert(sharedEvents), resultSharedEvents);

    }

    @Test
    public void shouldFindEvent() {
        Long eventId = 1L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        EventDto resultEvent = eventService.findEvent(eventId);

        assertEquals(eventId, resultEvent.getEventId());
    }

    @Test
    public void shouldThrowErrorWhenFindEventIsDeleted() {
        Long eventId = 1L;
        event.setDeleted(true);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatExceptionOfType(ForbiddenException.class)
                .isThrownBy(() -> {
                    eventService.findEvent(eventId);
                }).withMessage("The event has been deleted and is archived");
    }
}
