package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Event;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventDtoListMapper implements Converter<List<EventDto>, List<Event>> {

    @Override
    public List<EventDto> convert(List<Event> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        List<EventDto> eventDtoList = new ArrayList<>();

        for (Event event : from) {
            EventDto eventDto = EventDto.builder()
                    .eventId(event.getEventId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .image(ImageDto.builder()
                            .filename(event.getImage().getFilename())
                            .url("localhost:8080/api/images/" + event.getImage().getImageId())
                            .type(event.getImage().getType())
                            .build())
                    .eventDate(event.getEventDate().format(formatter))
                    .createdAt(event.getCreatedAt().format(formatter))
                    .eventCreatorName(event.getEventCreator().getUserProfile().getFirstName()
                            + " " + event.getEventCreator().getUserProfile().getLastName())
                    .authorId(event.getEventCreator().getUserId())
                    .eventAddress(AddressDto.builder()
                            .country(event.getEventAddress().getCountry())
                            .city(event.getEventAddress().getCity())
                            .street(event.getEventAddress().getStreet())
                            .zipCode(event.getEventAddress().getZipCode())
                            .build())
                    .members(
                            event.getMembers().stream()
                                    .map(member -> EventMemberDto.builder()
                                            .userId(member.getEventMember().getUserId())
                                            .eventMemberName(member.getEventMember().getUserProfile().getFirstName()
                                                    + " " + member.getEventMember().getUserProfile().getLastName())
                                            .participationStatus(member.getParticipationStatus())
                                            .addedIn(member.getAddedIn().format(formatter))
                                            .invitationDate(member.getInvitationDate().format(formatter))
                                            .invitationDisplayed(member.isInvitationDisplayed())
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .sharing(
                            event.getSharing().stream()
                                    .map(sharedEvent -> SharedEventInfoDto.builder()
                                            .userId(sharedEvent.getSharedEventUser().getUserId())
                                            .authorOfSharing(sharedEvent.getSharedEventUser().getUserProfile().getFirstName()
                                                    + " " + sharedEvent.getSharedEventUser().getUserProfile().getLastName())
                                            .sharingDate(sharedEvent.getDate().format(formatter))
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .build();
            eventDtoList.add(eventDto);
        }
        return eventDtoList;
    }
}