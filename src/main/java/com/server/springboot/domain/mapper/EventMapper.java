package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestEventDto;
import com.server.springboot.domain.entity.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EventMapper implements Converter<Event, RequestEventDto> {

    @Override
    public Event convert(RequestEventDto from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return Event.builder()
                .title(from.getTitle())
                .description(from.getDescription())
                .eventDate(LocalDateTime.parse(from.getEventDate(), formatter))
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }
}

