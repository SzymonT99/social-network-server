package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Event;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class EventDtoMapper implements Converter<EventDto, Event> {

    @Override
    public EventDto convert(Event from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        return EventDto.builder()
                .eventId(from.getEventId())
                .title(from.getTitle())
                .description(from.getDescription())
                .image(ImageDto.builder()
                        .filename(from.getImage().getFilename())
                        .url("localhost:8080/api/images/" + from.getImage().getImageId())
                        .type(from.getImage().getType())
                        .build())
                .eventDate(from.getEventDate().format(formatter))
                .createdAt(from.getCreatedAt().format(formatter))
                .eventCreatorName(from.getEventCreator().getUserProfile().getFirstName()
                        + " " + from.getEventCreator().getUserProfile().getLastName())
                .authorId(from.getEventCreator().getUserId())
                .eventAddress(AddressDto.builder()
                        .country(from.getEventAddress().getCountry())
                        .city(from.getEventAddress().getCity())
                        .street(from.getEventAddress().getStreet())
                        .zipCode(from.getEventAddress().getZipCode())
                        .build())
                .members(
                        from.getMembers().stream()
                                .map(member -> EventMemberDto.builder()
                                        .userId(member.getEventMember().getUserId())
                                        .eventMemberName(member.getEventMember().getUserProfile().getFirstName()
                                                + " " + member.getEventMember().getUserProfile().getLastName())
                                        .participationStatus(member.getParticipationStatus().toString())
                                        .addedIn(member.getAddedIn().format(formatter))
                                        .invitationDate(member.getInvitationDate().format(formatter))
                                        .invitationDisplayed(member.isInvitationDisplayed())
                                        .build())
                                .collect(Collectors.toList())
                )
                .sharing(
                        from.getSharing().stream()
                                .map(sharedEvent -> SharedEventInfoDto.builder()
                                        .userId(sharedEvent.getSharedEventUser().getUserId())
                                        .authorOfSharing(sharedEvent.getSharedEventUser().getUserProfile().getFirstName()
                                                + " " + sharedEvent.getSharedEventUser().getUserProfile().getLastName())
                                        .sharingDate(sharedEvent.getDate().format(formatter))
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}