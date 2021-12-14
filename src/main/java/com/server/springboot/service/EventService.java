package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestEventDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface EventService {

    void addEvent(RequestEventDto requestEventDto, MultipartFile imageFile);

    @Transactional
    void editEvent(Long eventId, RequestEventDto requestEventDto, MultipartFile imageFile);

    void deleteEventById(Long eventId, Long authorId);

    void deleteEventByIdWithArchiving(Long eventId, Long authorId, boolean archive);
}
