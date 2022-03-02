package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventDtoListMapper implements Converter<List<EventDto>, List<Event>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<AddressDto, Address> addressDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<EventMemberDto>, List<EventMember>> eventMemberDtoListMapper;

    @Autowired
    public EventDtoListMapper(Converter<ImageDto, Image> imageDtoMapper, Converter<AddressDto, Address> addressDtoMapper,
                              Converter<UserDto, User> userDtoMapper,
                              Converter<List<EventMemberDto>, List<EventMember>> eventMemberDtoListMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.addressDtoMapper = addressDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.eventMemberDtoListMapper = eventMemberDtoListMapper;
    }

    @Override
    public List<EventDto> convert(List<Event> from) {
        List<EventDto> eventDtoList = new ArrayList<>();

        for (Event event : from) {
            EventDto eventDto = EventDto.builder()
                    .eventId(event.getEventId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .image(event.getImage() != null ? imageDtoMapper.convert(event.getImage()) : null)
                    .eventDate(event.getEventDate().toString())
                    .createdAt(event.getCreatedAt().toString())
                    .eventAuthor(userDtoMapper.convert(event.getEventCreator()))
                    .eventAddress(addressDtoMapper.convert(event.getEventAddress()))
                    .members(eventMemberDtoListMapper.convert(Lists.newArrayList(event.getMembers())))
                    .sharing(
                            event.getSharing().stream()
                                    .map(sharedEvent -> SharedEventInfoDto.builder()
                                            .authorOfSharing(userDtoMapper.convert(sharedEvent.getSharedEventUser()))
                                            .sharingDate(sharedEvent.getDate().toString())
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .build();
            eventDtoList.add(eventDto);
        }
        return eventDtoList;
    }
}