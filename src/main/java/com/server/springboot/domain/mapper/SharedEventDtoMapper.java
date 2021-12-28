package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Event;
import com.server.springboot.domain.entity.SharedEvent;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class SharedEventDtoMapper implements Converter<SharedEventDto, SharedEvent> {

    private final Converter<EventDto, Event> eventDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public SharedEventDtoMapper(Converter<EventDto, Event> eventDtoMapper, Converter<UserDto, User> userDtoMapper) {
        this.eventDtoMapper = eventDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public SharedEventDto convert(SharedEvent from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return SharedEventDto.builder()
                .authorOfSharing(userDtoMapper.convert(from.getSharedEventUser()))
                .sharingDate(from.getDate().format(formatter))
                .event(eventDtoMapper.convert(from.getEvent()))
                .build();
    }
}
