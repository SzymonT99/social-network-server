package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.EventInvitationDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {

    void addEvent(RequestEventDto requestEventDto, MultipartFile imageFile);

    @Transactional
    void editEvent(Long eventId, RequestEventDto requestEventDto, MultipartFile imageFile);

    void deleteEventById(Long eventId, Long authorId);

    void deleteEventByIdWithArchiving(Long eventId, Long authorId, boolean archive);

    List<EventDto> findAllEvents();

    void inviteUser(Long eventId, Long userId);

    List<EventInvitationDto> findAllUserEventInvitation(Long userId);

    void respondToEvent(Long eventId, Long userId, String reactionToEvent);

    void shareEvent(Long eventId, Long userId);

    @Transactional
    void deleteSharedEvent(Long eventId, Long userId);

    List<SharedEventDto>  findAllSharedEventsByUser(Long userId);
}
