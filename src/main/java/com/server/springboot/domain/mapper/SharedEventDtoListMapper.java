package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.EventDto;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Event;
import com.server.springboot.domain.entity.SharedEvent;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SharedEventDtoListMapper implements Converter<List<SharedEventDto>, List<SharedEvent>> {

    private final Converter<EventDto, Event> eventDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public SharedEventDtoListMapper() {
        this.eventDtoMapper = new EventDtoMapper();
        this.userDtoMapper = new UserDtoMapper();
    }

    @Override
    public List<SharedEventDto> convert(List<SharedEvent> from) {
        List<SharedEventDto> sharedEventDtoList = new ArrayList<>();

        from = from.stream()
                .sorted(Comparator.comparing(SharedEvent::getDate).reversed())
                .collect(Collectors.toList());

        for (SharedEvent sharedEvent : from) {
            SharedEventDto sharedEventDto = SharedEventDto.builder()
                    .authorOfSharing(userDtoMapper.convert(sharedEvent.getSharedEventUser()))
                    .sharingDate(sharedEvent.getDate().toString())
                    .event(eventDtoMapper.convert(sharedEvent.getEvent()))
                    .build();
            sharedEventDtoList.add(sharedEventDto);
        }
        return sharedEventDtoList;
    }
}
