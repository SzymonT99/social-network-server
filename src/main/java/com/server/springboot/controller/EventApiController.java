package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestEventDto;
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

}


