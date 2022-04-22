package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.EventInvitationDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.key.UserEventKey;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.EventParticipationStatus;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.EventService;
import com.server.springboot.service.FileService;
import com.server.springboot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final FileService fileService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventMemberRepository eventMemberRepository;
    private final AddressRepository addressRepository;
    private final ImageRepository imageRepository;
    private final SharedEventRepository sharedEventRepository;
    private final JwtUtils jwtUtils;
    private final EventMapper eventMapper;
    private final AddressMapper addressMapper;
    private final EventDtoMapper eventDtoMapper;
    private final EventDtoListMapper eventDtoListMapper;
    private final EventInvitationDtoListMapper eventInvitationDtoListMapper;
    private final SharedEventDtoListMapper sharedEventDtoListMapper;
    private final NotificationService notificationService;
    private final RoleRepository roleRepository;

    @Autowired
    public EventServiceImpl(FileService fileService, UserRepository userRepository, EventRepository eventRepository,
                            EventMemberRepository eventMemberRepository, AddressRepository addressRepository, ImageRepository imageRepository,
                            SharedEventRepository sharedEventRepository, JwtUtils jwtUtils,
                            EventMapper eventMapper, AddressMapper addressMapper,
                            EventDtoMapper eventDtoMapper, EventDtoListMapper eventDtoListMapper,
                            EventInvitationDtoListMapper eventInvitationDtoListMapper,
                            SharedEventDtoListMapper sharedEventDtoListMapper, NotificationService notificationService,
                            RoleRepository roleRepository) {
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventMemberRepository = eventMemberRepository;
        this.addressRepository = addressRepository;
        this.imageRepository = imageRepository;
        this.sharedEventRepository = sharedEventRepository;
        this.jwtUtils = jwtUtils;
        this.eventMapper = eventMapper;
        this.addressMapper = addressMapper;
        this.eventDtoMapper = eventDtoMapper;
        this.eventDtoListMapper = eventDtoListMapper;
        this.eventInvitationDtoListMapper = eventInvitationDtoListMapper;
        this.sharedEventDtoListMapper = sharedEventDtoListMapper;
        this.notificationService = notificationService;
        this.roleRepository = roleRepository;
    }

    @Override
    public EventDto createEvent(RequestEventDto requestEventDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedInUserId();
        User eventAuthor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event createdEvent = eventMapper.convert(requestEventDto);
        createdEvent.setEventCreator(eventAuthor);

        if (imageFile != null) {
            Image image = fileService.storageOneImage(imageFile, eventAuthor, false);
            createdEvent.setImage(image);
        }

        Address eventAddress = addressMapper.convert(requestEventDto.getEventAddress());
        addressRepository.save(eventAddress);
        createdEvent.setEventAddress(eventAddress);

        eventRepository.save(createdEvent);

        return eventDtoMapper.convert(createdEvent);
    }

    @Override
    public void editEvent(Long eventId, RequestEventDto requestEventDto, MultipartFile imageFile) throws IOException {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        if (!event.getEventCreator().getUserId().equals(userId)
                && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Event editing access forbidden");
        }

        if (event.getImage() != null) {
            String lastImageId = event.getImage().getImageId();
            event.setImage(null);
            imageRepository.deleteByImageId(lastImageId);
            fileService.deleteImage(lastImageId);
        }

        if (imageFile != null) {
            Image updatedImages = fileService.storageOneImage(imageFile, event.getEventCreator(), false);
            event.setImage(updatedImages);
        }

        event.setTitle(requestEventDto.getTitle());
        event.setDescription(requestEventDto.getDescription());

        Address updatedAddress = event.getEventAddress();
        updatedAddress.setCountry(requestEventDto.getEventAddress().getCountry());
        updatedAddress.setCity(requestEventDto.getEventAddress().getCity());
        updatedAddress.setStreet(requestEventDto.getEventAddress().getStreet());
        updatedAddress.setZipCode(requestEventDto.getEventAddress().getZipCode());
        event.setEventAddress(updatedAddress);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        event.setEventDate(LocalDateTime.parse(requestEventDto.getEventDate(), formatter));

        eventRepository.save(event);
    }

    @Override
    public void deleteEventById(Long eventId, boolean archive) throws IOException {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        if (!event.getEventCreator().getUserId().equals(userId)
                && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Event deleting access forbidden");
        }

        if (archive) {
            event.setDeleted(true);
            eventRepository.save(event);
        } else {
            eventRepository.deleteByEventId(eventId);
            if (event.getImage() != null) {
                fileService.deleteImage(event.getImage().getImageId());
            }
        }
    }

    @Override
    public List<EventDto> findAllEvents() {
        List<Event> events = eventRepository.findByIsDeletedOrderByCreatedAtDesc(false);
        return eventDtoListMapper.convert(events);
    }

    @Override
    public void inviteUser(Long eventId, Long invitedUserId) {
        Long loggedUserId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));

        User user = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + invitedUserId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));

        if (eventMemberRepository.existsByEventMemberAndEvent(user, event)) {
            throw new ConflictRequestException("The user has already reacted to the event or an invitation has already been sent to him");
        }

        EventMember eventMember = EventMember.builder()
                .participationStatus(EventParticipationStatus.INVITED)
                .invitationDisplayed(false)
                .invitationDate(LocalDateTime.now())
                .eventMember(user)
                .event(event)
                .build();
        eventMemberRepository.save(eventMember);

        notificationService.sendNotificationToUser(loggedUser, invitedUserId, ActionType.ACTIVITY_BOARD);
    }

    @Override
    public List<EventInvitationDto> findAllUserEventInvitation(boolean isDisplayed) {
        Long loggedUserId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        List<EventMember> eventMembers = eventMemberRepository.findByEventMemberAndParticipationStatus(user, EventParticipationStatus.INVITED);
        if (isDisplayed) {
            eventMemberRepository.setEventInvitationDisplayed(true, user);
        }
        return eventInvitationDtoListMapper.convert(eventMembers);
    }

    @Override
    public void respondToEvent(Long eventId, String reactionToEvent) {
        List<String> eventParticipationStatusList = Arrays.stream(EventParticipationStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        if (!eventParticipationStatusList.contains(reactionToEvent)) {
            throw new BadRequestException("Unknown reaction to event");
        }

        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));

        if (eventMemberRepository.existsByEventMemberAndEvent(user, event)) {
            EventMember updatedEventMember = eventMemberRepository.findByEventMemberAndEvent(user, event).get();
            if (updatedEventMember.getParticipationStatus() == EventParticipationStatus.valueOf(reactionToEvent)) {
                throw new ConflictRequestException("The user has already sent the same reaction to this event");
            }

            updatedEventMember.setParticipationStatus(EventParticipationStatus.valueOf(reactionToEvent));
            if (EventParticipationStatus.valueOf(reactionToEvent) != EventParticipationStatus.TAKE_PART
                    && EventParticipationStatus.valueOf(reactionToEvent) != EventParticipationStatus.INTERESTED) {
                updatedEventMember.setAddedIn(null);
            } else {
                updatedEventMember.setAddedIn(LocalDateTime.now());
            }
            eventMemberRepository.save(updatedEventMember);

        } else {
            EventMember mewEventMember = EventMember.builder()
                    .eventMember(user)
                    .event(event)
                    .participationStatus(EventParticipationStatus.valueOf(reactionToEvent))
                    .addedIn(LocalDateTime.now())
                    .build();
            if (EventParticipationStatus.valueOf(reactionToEvent) == EventParticipationStatus.TAKE_PART
                    || EventParticipationStatus.valueOf(reactionToEvent) == EventParticipationStatus.INTERESTED) {
                mewEventMember.setAddedIn(LocalDateTime.now());
            }
            eventMemberRepository.save(mewEventMember);
        }
    }

    @Override
    public void shareEvent(Long eventId) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));

        if (sharedEventRepository.existsBySharedEventUserAndEvent(user, event)) {
            throw new ForbiddenException("The user has already shared this event");
        }

        SharedEvent sharedEvent = SharedEvent.builder()
                .id(UserEventKey.builder().eventId(eventId).userId(userId).build())
                .sharedEventUser(user)
                .event(event)
                .date(LocalDateTime.now())
                .build();
        sharedEventRepository.save(sharedEvent);
    }

    @Override
    public void deleteSharedEvent(Long eventId) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));

        if (!sharedEventRepository.existsBySharedEventUserAndEvent(user, event)
                && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new NotFoundException("Not found shared event where:  user id: " + userId + " and event id: " + eventId);
        }

        SharedEvent sharedEvent = sharedEventRepository.findBySharedEventUserAndEvent(user, event).get();
        sharedEventRepository.delete(sharedEvent);
    }

    @Override
    public List<SharedEventDto> findAllSharedEventsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<SharedEvent> sharedEvents = sharedEventRepository.findBySharedEventUser(user);
        return sharedEventDtoListMapper.convert(sharedEvents);
    }

    @Override
    public EventDto findEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));

        if (event.isDeleted()) {
            throw new ForbiddenException("The event has been deleted and is archived");
        }

        return eventDtoMapper.convert(event);
    }
}
