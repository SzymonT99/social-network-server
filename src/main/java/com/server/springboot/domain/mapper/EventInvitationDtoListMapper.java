package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Address;
import com.server.springboot.domain.entity.EventMember;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventInvitationDtoListMapper implements Converter<List<EventInvitationDto>, List<EventMember>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<AddressDto, Address> addressDtoMapper;
    private final Converter<List<EventMemberDto>, List<EventMember>> eventMemberDtoListMapper;

    @Autowired
    public EventInvitationDtoListMapper() {
        this.imageDtoMapper = new ImageDtoMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.addressDtoMapper = new AddressDtoMapper();
        this.eventMemberDtoListMapper = new EventMemberDtoListMapper();
    }

    @Override
    public List<EventInvitationDto> convert(List<EventMember> from) {
        List<EventInvitationDto> eventInvitationDtoList = new ArrayList<>();

        for (EventMember eventMember : from) {
            EventInvitationDto eventDto = EventInvitationDto.builder()
                    .invitationDisplayed(eventMember.isInvitationDisplayed())
                    .invitationDate(eventMember.getInvitationDate().toString())
                    .eventId(eventMember.getEvent().getEventId())
                    .title(eventMember.getEvent().getTitle())
                    .description(eventMember.getEvent().getDescription())
                    .image(eventMember.getEvent().getImage() != null
                            ? imageDtoMapper.convert(eventMember.getEvent().getImage()) : null)
                    .eventDate(eventMember.getEvent().getEventDate().toString())
                    .createdAt(eventMember.getEvent().getCreatedAt().toString())
                    .eventAuthor(userDtoMapper.convert(eventMember.getEvent().getEventCreator()))
                    .eventAddress(addressDtoMapper.convert(eventMember.getEvent().getEventAddress()))
                    .eventMembers(eventMemberDtoListMapper.convert(Lists.newArrayList(eventMember.getEvent().getMembers())))
                    .build();
            eventInvitationDtoList.add(eventDto);
        }
        return eventInvitationDtoList;
    }
}