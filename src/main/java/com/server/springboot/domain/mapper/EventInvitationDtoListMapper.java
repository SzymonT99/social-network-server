package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Event;
import com.server.springboot.domain.entity.EventMember;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventInvitationDtoListMapper implements Converter<List<EventInvitationDto>, List<EventMember>> {

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
                    .image(ImageDto.builder()
                            .filename(eventMember.getEvent().getImage().getFilename())
                            .url("localhost:8080/api/images/" + eventMember.getEvent().getImage().getImageId())
                            .type(eventMember.getEvent().getImage().getType())
                            .build())
                    .eventDate(eventMember.getEvent().getEventDate().format(formatter))
                    .createdAt(eventMember.getEvent().getCreatedAt().format(formatter))
                    .eventCreatorName(eventMember.getEvent().getEventCreator().getUserProfile().getFirstName()
                            + " " + eventMember.getEvent().getEventCreator().getUserProfile().getLastName())
                    .authorId(eventMember.getEvent().getEventCreator().getUserId())
                    .eventAddress(AddressDto.builder()
                            .country(eventMember.getEvent().getEventAddress().getCountry())
                            .city(eventMember.getEvent().getEventAddress().getCity())
                            .street(eventMember.getEvent().getEventAddress().getStreet())
                            .zipCode(eventMember.getEvent().getEventAddress().getZipCode())
                            .build())
                    .build();
            eventInvitationDtoList.add(eventDto);
        }
        return eventInvitationDtoList;
    }
}