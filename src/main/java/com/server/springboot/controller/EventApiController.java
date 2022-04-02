package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.EventInvitationDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.service.EventService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class EventApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final EventService eventService;

    @Autowired
    public EventApiController(EventService eventService) {
        this.eventService = eventService;
    }

    @ApiOperation(value = "Create an event")
    @PostMapping(value = "/events", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EventDto> createEvent(@RequestPart(value = "image", required = false) MultipartFile imageFile,
                                                @Valid @RequestPart(value = "event") RequestEventDto requestEventDto) {
        LOGGER.info("---- Create event with title: {}", requestEventDto.getTitle());
        return new ResponseEntity<>(eventService.addEvent(requestEventDto, imageFile), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing event by id")
    @PutMapping(value = "/events/{eventId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateEvent(@PathVariable(value = "eventId") Long eventId,
                                         @RequestPart(value = "image", required = false) MultipartFile imageFile,
                                         @Valid @RequestPart(value = "event") RequestEventDto requestEventDto) throws IOException {
        LOGGER.info("---- Update event with id: {} and title: {}", eventId, requestEventDto.getTitle());
        eventService.editEvent(eventId, requestEventDto, imageFile);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete an event by id")
    @DeleteMapping(value = "/events/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable(value = "eventId") Long eventId,
                                         @RequestParam(value = "archive") boolean archive) throws IOException {
        LOGGER.info("---- Delete event with id: {}", eventId);
        eventService.deleteEventById(eventId, archive);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get event by id")
    @GetMapping(value = "/events/{eventId}")
    public ResponseEntity<EventDto> getAllEvents(@PathVariable(value = "eventId") Long eventId) {
        LOGGER.info("---- Get event by id");
        return new ResponseEntity<>(eventService.findEvent(eventId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get all events")
    @GetMapping(value = "/events")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        LOGGER.info("---- Get all events");
        return new ResponseEntity<>(eventService.findAllEvents(), HttpStatus.OK);
    }

    @ApiOperation(value = "Invite user to events")
    @PostMapping(value = "/events/{eventId}/invite")
    public ResponseEntity<?> inviteForEvent(@PathVariable(value = "eventId") Long eventId,
                                            @RequestParam(value = "invitedUserId") Long invitedUserId) {
        LOGGER.info("---- Invitation to event for user with id: {}", invitedUserId);
        eventService.inviteUser(eventId, invitedUserId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get user's event invitations")
    @GetMapping(value = "/events/invitations")
    public ResponseEntity<List<EventInvitationDto>> getUserInvitationForEvent() {
        LOGGER.info("---- Get all user invitations to event");
        return new ResponseEntity<>(eventService.findAllUserEventInvitation(false), HttpStatus.OK);
    }

    @ApiOperation(value = "Respond to event")
    @PutMapping(value = "/events/{eventId}/response")
    public ResponseEntity<?> respondToEvent(@PathVariable(value = "eventId") Long eventId,
                                            @RequestParam(value = "reaction") String reactionToEvent) {
        LOGGER.info("----User reactions to an event: {}", reactionToEvent);
        eventService.respondToEvent(eventId, reactionToEvent);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Share event")
    @PostMapping(value = "/events/{eventId}/share")
    public ResponseEntity<?> shareEvent(@PathVariable(value = "eventId") Long eventId) {
        LOGGER.info("---- User shares event with id: {}", eventId);
        eventService.shareEvent(eventId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete shared event")
    @DeleteMapping(value = "/events/{eventId}/shared")
    public ResponseEntity<?> deleteSharedEvent(@PathVariable(value = "eventId") Long eventId) {
        LOGGER.info("---- User deletes shared event with id: {}", eventId);
        eventService.deleteSharedEvent(eventId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get user's shared event")
    @GetMapping(value = "/events/shared")
    public ResponseEntity<List<SharedEventDto>> getAllUserSharedEvents(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Get all shared user shared events");
        return new ResponseEntity<>(eventService.findAllSharedEventsByUser(userId), HttpStatus.OK);
    }
}


