package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.EventReactionDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Event;
import com.server.springboot.domain.entity.EventMember;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.EventParticipationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class EventReactionDtoMapper implements Converter<EventReactionDto, EventMember>{

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<EventDto, Event>  eventDtoMapper;

    @Autowired
    public EventReactionDtoMapper(Converter<UserDto, User> userDtoMapper, Converter<EventDto, Event> eventDtoMapper) {
        this.userDtoMapper = userDtoMapper;
        this.eventDtoMapper = eventDtoMapper;
    }

    @Override
    public EventReactionDto convert(EventMember from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return EventReactionDto.builder()
                .eventMember(userDtoMapper.convert(from.getEventMember()))
                .participationStatus(from.getParticipationStatus())
                .addedIn(from.getAddedIn() != null ? from.getAddedIn().format(formatter) : null)
                .event(eventDtoMapper.convert(from.getEvent()))
                .build();
    }
}