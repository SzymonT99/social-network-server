package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventDtoMapper implements Converter<EventDto, Event> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<AddressDto, Address> addressDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<EventMemberDto>, List<EventMember>> eventMemberDtoListMapper;

    @Autowired
    public EventDtoMapper(Converter<ImageDto, Image> imageDtoMapper, Converter<AddressDto, Address> addressDtoMapper,
                          Converter<UserDto, User> userDtoMapper, Converter<List<EventMemberDto>, List<EventMember>> eventMemberDtoListMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.addressDtoMapper = addressDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.eventMemberDtoListMapper = eventMemberDtoListMapper;
    }

    @Override
    public EventDto convert(Event from) {

        return EventDto.builder()
                .eventId(from.getEventId())
                .title(from.getTitle())
                .description(from.getDescription())
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .eventDate(from.getEventDate().toString())
                .createdAt(from.getCreatedAt().toString())
                .eventAuthor(userDtoMapper.convert(from.getEventCreator()))
                .eventAddress(addressDtoMapper.convert(from.getEventAddress()))
                .members(eventMemberDtoListMapper.convert(Lists.newArrayList(from.getMembers())))
                .sharing(
                        from.getSharing().stream()
                                .map(sharedEvent -> SharedEventInfoDto.builder()
                                        .authorOfSharing(userDtoMapper.convert(sharedEvent.getSharedEventUser()))
                                        .sharingDate(sharedEvent.getDate().toString())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}