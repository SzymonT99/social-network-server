package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Address;
import com.server.springboot.domain.entity.EventMember;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventInvitationDtoListMapper implements Converter<List<EventInvitationDto>, List<EventMember>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<AddressDto, Address> addressDtoMapper;

    @Autowired
    public EventInvitationDtoListMapper(Converter<ImageDto, Image> imageDtoMapper, Converter<UserDto, User> userDtoMapper,
                                        Converter<AddressDto, Address> addressDtoMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.addressDtoMapper = addressDtoMapper;
    }

    @Override
    public List<EventInvitationDto> convert(List<EventMember> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        List<EventInvitationDto> eventInvitationDtoList = new ArrayList<>();

        for (EventMember eventMember : from) {
            EventInvitationDto eventDto = EventInvitationDto.builder()
                    .invitationDisplayed(eventMember.isInvitationDisplayed())
                    .invitationDate(eventMember.getInvitationDate().format(formatter))
                    .eventId(eventMember.getEvent().getEventId())
                    .title(eventMember.getEvent().getTitle())
                    .description(eventMember.getEvent().getDescription())
                    .image(imageDtoMapper.convert(eventMember.getEvent().getImage()))
                    .eventDate(eventMember.getEvent().getEventDate().format(formatter))
                    .createdAt(eventMember.getEvent().getCreatedAt().format(formatter))
                    .eventAuthor(userDtoMapper.convert(eventMember.getEvent().getEventCreator()))
                    .eventAddress(addressDtoMapper.convert(eventMember.getEvent().getEventAddress()))
                    .build();
            eventInvitationDtoList.add(eventDto);
        }
        return eventInvitationDtoList;
    }
}