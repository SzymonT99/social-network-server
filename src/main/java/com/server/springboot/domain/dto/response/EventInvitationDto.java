package com.server.springboot.domain.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class EventInvitationDto {
    private boolean invitationDisplayed;
    private String invitationDate;
    private Long eventId;
    private String title;
    private String description;
    private ImageDto image;
    private String eventDate;
    private String createdAt;
    private UserDto eventAuthor;
    private AddressDto eventAddress;
    private List<EventMemberDto> eventMembers;
}