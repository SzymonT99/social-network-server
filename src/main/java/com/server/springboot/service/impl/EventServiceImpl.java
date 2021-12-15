package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestAddressDto;
import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.EventInvitationDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.key.UserEventKey;
import com.server.springboot.domain.entity.key.UserPostKey;
import com.server.springboot.domain.enumeration.EventParticipationStatus;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.service.EventService;
import com.server.springboot.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final FileService fileService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventMemberRepository eventMemberRepository;
    private final AddressRepository addressRepository;
    private final ImageRepository imageRepository;
    private final SharedEventRepository sharedEventRepository;
    private final Converter<Event, RequestEventDto> eventMapper;
    private final Converter<Address, RequestAddressDto> addressMapper;
    private final Converter<List<EventDto>, List<Event>> eventDtoListMapper;
    private final Converter<List<EventInvitationDto>, List<EventMember>> eventInvitationDtoListMapper;
    private final Converter<List<SharedEventDto>, List<SharedEvent>> sharedEventDtoListMapper;

    @Autowired
    public EventServiceImpl(FileService fileService, UserRepository userRepository, EventRepository eventRepository,
                            EventMemberRepository eventMemberRepository, AddressRepository addressRepository, ImageRepository imageRepository,
                            SharedEventRepository sharedEventRepository, Converter<Event, RequestEventDto> eventMapper,
                            Converter<Address, RequestAddressDto> addressMapper, Converter<List<EventDto>, List<Event>> eventDtoListMapper,
                            Converter<List<EventInvitationDto>, List<EventMember>> eventInvitationDtoListMapper,
                            Converter<List<SharedEventDto>, List<SharedEvent>> sharedEventDtoListMapper) {
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventMemberRepository = eventMemberRepository;
        this.addressRepository = addressRepository;
        this.imageRepository = imageRepository;
        this.sharedEventRepository = sharedEventRepository;
        this.eventMapper = eventMapper;
        this.addressMapper = addressMapper;
        this.eventDtoListMapper = eventDtoListMapper;
        this.eventInvitationDtoListMapper = eventInvitationDtoListMapper;
        this.sharedEventDtoListMapper = sharedEventDtoListMapper;
    }

    @Override
    public void addEvent(RequestEventDto requestEventDto, MultipartFile imageFile) {
        User eventAuthor = userRepository.findById(requestEventDto.getUserId())
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requestEventDto.getUserId()));
        Event createdEvent = eventMapper.convert(requestEventDto);
        createdEvent.setEventCreator(eventAuthor);

        if (!imageFile.isEmpty()) {
            Image image = fileService.storageOneImage(imageFile, eventAuthor, false);
            createdEvent.setImage(image);
        }

        Address eventAddress = addressMapper.convert(requestEventDto.getEventAddress());
        addressRepository.save(eventAddress);
        createdEvent.setEventAddress(eventAddress);

        eventRepository.save(createdEvent);
    }

    @Override
    public void editEvent(Long eventId, RequestEventDto requestEventDto, MultipartFile imageFile) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        if (!event.getEventCreator().getUserId().equals(requestEventDto.getUserId())) {
            throw new ForbiddenException("Invalid event creator id - event editing access forbidden");
        }
        if (event.getImage() != null) {
            imageRepository.deleteByImageId(event.getImage().getImageId());
        }
        Image updatedImages = fileService.storageOneImage(imageFile, event.getEventCreator(), false);
        event.setImage(updatedImages);

        event.setTitle(requestEventDto.getTitle());
        event.setDescription(requestEventDto.getDescription());

        Address updatedAddress = event.getEventAddress();
        updatedAddress.setCountry(requestEventDto.getEventAddress().getCountry());
        updatedAddress.setCity(requestEventDto.getEventAddress().getCity());
        updatedAddress.setStreet(requestEventDto.getEventAddress().getStreet());
        updatedAddress.setZipCode(requestEventDto.getEventAddress().getZipCode());
        event.setEventAddress(updatedAddress);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        event.setEventDate(LocalDateTime.parse(requestEventDto.getEventDate(), formatter));

        eventRepository.save(event);
    }

    @Override
    public void deleteEventById(Long eventId, Long authorId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        if (!event.getEventCreator().getUserId().equals(authorId)) {
            throw new ForbiddenException("Invalid event creator id - event deleting access forbidden");
        }
        eventRepository.deleteByEventId(eventId);
    }

    @Override
    public void deleteEventByIdWithArchiving(Long eventId, Long authorId, boolean archive) {
        if (archive) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
            if (!event.getEventCreator().getUserId().equals(authorId)) {
                throw new ForbiddenException("Invalid event creator id - event deleting access forbidden");
            }
            event.setDeleted(true);
            eventRepository.save(event);
        } else {
            deleteEventById(eventId, authorId);
        }
    }

    @Override
    public List<EventDto> findAllEvents() {
        List<Event> events = eventRepository.findByIsDeletedOrderByCreatedAtDesc(false);
        return eventDtoListMapper.convert(events);
    }

    @Override
    public void inviteUser(Long eventId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        EventMember eventMember = EventMember.builder()
                .participationStatus(EventParticipationStatus.INVITED)
                .invitationDisplayed(false)
                .invitationDate(LocalDateTime.now())
                .eventMember(user)
                .event(event)
                .build();
        eventMemberRepository.save(eventMember);
    }

    @Override
    public List<EventInvitationDto> findAllUserEventInvitation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<EventMember> eventMembers = eventMemberRepository.findByEventMemberAndParticipationStatus(user, EventParticipationStatus.INVITED);
        return eventInvitationDtoListMapper.convert(eventMembers);
    }

    @Override
    public void respondToEvent(Long eventId, Long userId, String reactionToEvent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        EventMember eventMember = EventMember.builder()
                .eventMember(user)
                .event(event)
                .participationStatus(EventParticipationStatus.valueOf(reactionToEvent))
                .addedIn(LocalDateTime.now())
                .build();
        if (EventParticipationStatus.valueOf(reactionToEvent) == EventParticipationStatus.TAKE_PART) {
            eventMember.setAddedIn(LocalDateTime.now());
        }
        eventMemberRepository.save(eventMember);
    }

    @Override
    public void shareEvent(Long eventId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        SharedEvent sharedEvent = SharedEvent.builder()
                .id(UserEventKey.builder().eventId(eventId).userId(userId).build())
                .sharedEventUser(user)
                .event(event)
                .date(LocalDateTime.now())
                .build();
        sharedEventRepository.save(sharedEvent);
    }

    @Override
    public void deleteSharedEvent(Long eventId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id: " + eventId));
        SharedEvent sharedEvent = sharedEventRepository.findBySharedEventUserAndEvent(user, event)
                .orElseThrow(() -> new NotFoundException("Not found shared event where:  user id: " + userId + " and event id: " + eventId));
        sharedEventRepository.delete(sharedEvent);
    }

    @Override
    public List<SharedEventDto> findAllSharedEventsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<SharedEvent> sharedEvents = sharedEventRepository.findBySharedEventUser(user);
        return sharedEventDtoListMapper.convert(sharedEvents);
    }
}