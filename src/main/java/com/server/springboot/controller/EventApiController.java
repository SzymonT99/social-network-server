package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.EventInvitationDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class EventApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationApiController.class);
    private final EventService eventService;

    @Autowired
    public EventApiController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping(value = "/events", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createEvent(@RequestPart(value = "image") MultipartFile imageFile,
                                         @Valid @RequestPart(value = "event") RequestEventDto requestEventDto) {
        LOGGER.info("---- Create event with title: {}", requestEventDto.getTitle());
        eventService.addEvent(requestEventDto, imageFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/events/{eventId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateEvent(@PathVariable(value = "eventId") Long eventId,
                                        @RequestPart(value = "image") MultipartFile imageFile,
                                        @Valid @RequestPart(value = "event") RequestEventDto requestEventDto) {
        LOGGER.info("---- Update event with id: {} and title: {}", eventId, requestEventDto.getTitle());
        eventService.editEvent(eventId, requestEventDto, imageFile);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/events/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable(value = "eventId") Long eventId, @RequestParam(value = "authorId") Long authorId) {
        LOGGER.info("---- Delete event with id: {}", eventId);
        eventService.deleteEventById(eventId, authorId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/events")
    public ResponseEntity<?> deleteEventWithArchiving(@RequestParam(value = "eventId") Long eventId, @RequestParam(value = "authorId") Long authorId,
                                        @RequestParam(value = "archive") @NotNull boolean archive) {
        LOGGER.info("---- Delete event by archiving with id: {}", eventId);
        eventService.deleteEventByIdWithArchiving(eventId, authorId, archive);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/events")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        LOGGER.info("---- Get all events");
        return new ResponseEntity<>(eventService.findAllEvents(), HttpStatus.OK);
    }

    @PostMapping(value = "/events/{eventId}/invite")
    public ResponseEntity<?> createEvent(@PathVariable(value = "eventId") Long eventId, @RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Invitation to event for user with id: {}", userId);
        eventService.inviteUser(eventId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/events-invitations")
    public ResponseEntity<List<EventInvitationDto>> getUserInvitationForEvent(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Get all user with id: {} invitation to event", userId);
        return new ResponseEntity<>(eventService.findAllUserEventInvitation(userId), HttpStatus.OK);
    }

    @PutMapping(value = "/events/{eventId}/response")
    public ResponseEntity<?> respondToEvent(@PathVariable(value = "eventId") Long eventId,
                                        @RequestParam(value = "userId") Long userId,
                                        @RequestParam(value = "reaction") String reactionToEvent) {
        LOGGER.info("----User with id: {} reaction to an event: {}", userId, reactionToEvent);
        eventService.respondToEvent(eventId, userId, reactionToEvent);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/events/{eventId}/share")
    public ResponseEntity<?> shareEvent(@PathVariable(value = "eventId") Long eventId, @RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- User with id: {} share event with id: {}", userId, eventId);
        eventService.shareEvent(eventId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/shared-events")
    public ResponseEntity<?> deleteSharedEvent(@RequestParam(value = "eventId") Long eventId, @RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Deleted shared event");
        eventService.deleteSharedEvent(eventId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/shared-events")
    public ResponseEntity<List<SharedEventDto>> getAllUserSharedEvents(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Get all shared user shared events");
        return new ResponseEntity<>(eventService.findAllSharedEventsByUser(userId), HttpStatus.OK);
    }

}


