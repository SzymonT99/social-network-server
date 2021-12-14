package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.AddressDto;
import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.AddressRepository;
import com.server.springboot.domain.repository.EventRepository;
import com.server.springboot.domain.repository.ImageRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.service.EventService;
import com.server.springboot.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EventServiceImpl implements EventService {

    private final FileService fileService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AddressRepository addressRepository;
    private final ImageRepository imageRepository;
    private final Converter<Event, RequestEventDto> eventMapper;
    private final Converter<Address, AddressDto> addressMapper;

    @Autowired
    public EventServiceImpl(FileService fileService, UserRepository userRepository, EventRepository eventRepository,
                            AddressRepository addressRepository, ImageRepository imageRepository,
                            Converter<Event, RequestEventDto> eventMapper, Converter<Address, AddressDto> addressMapper) {
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.addressRepository = addressRepository;
        this.imageRepository = imageRepository;
        this.eventMapper = eventMapper;
        this.addressMapper = addressMapper;
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
            System.out.println("ELOOOOOOOOOOOOOOOOOOOOOOOO");
            System.out.println(event.getImage());
            System.out.println(event.getImage().getImageId());
            System.out.println(imageRepository.existsById(event.getImage().getImageId()));
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
        //TODO: usuwanie członków i udostępnień wydarzenia
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
            //TODO: usuwanie członków i udostępnień wydarzenia
        } else {
            deleteEventById(eventId, authorId);
        }
    }
}

