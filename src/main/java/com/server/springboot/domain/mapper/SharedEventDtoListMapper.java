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
import java.util.ArrayList;
import java.util.List;

@Component
public class SharedEventDtoListMapper implements Converter<List<SharedEventDto>, List<SharedEvent>> {

    private final Converter<EventDto, Event> eventDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public SharedEventDtoListMapper(Converter<EventDto, Event> eventDtoMapper, Converter<UserDto, User> userDtoMapper) {
        this.eventDtoMapper = eventDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<SharedEventDto> convert(List<SharedEvent> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        List<SharedEventDto> sharedEventDtoList = new ArrayList<>();

        for (SharedEvent sharedEvent : from) {
            SharedEventDto sharedEventDto = SharedEventDto.builder()
                    .authorOfSharing(userDtoMapper.convert(sharedEvent.getSharedEventUser()))
                    .sharingDate(sharedEvent.getDate().format(formatter))
                    .event(eventDtoMapper.convert(sharedEvent.getEvent()))
                    .build();
            sharedEventDtoList.add(sharedEventDto);
        }
        return sharedEventDtoList;
    }
}
