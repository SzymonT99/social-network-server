package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.EventMemberDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.EventMember;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventMemberDtoListMapper implements Converter<List<EventMemberDto>, List<EventMember>> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public EventMemberDtoListMapper() {
        this.userDtoMapper = new UserDtoMapper();
    }

    @Override
    public List<EventMemberDto> convert(List<EventMember> from) {
        List<EventMemberDto> eventMemberDtoList = new ArrayList<>();

        for (EventMember eventMember : from) {
            EventMemberDto eventMemberDto = EventMemberDto.builder()
                    .eventMember(userDtoMapper.convert(eventMember.getEventMember()))
                    .participationStatus(eventMember.getParticipationStatus())
                    .addedIn(eventMember.getAddedIn() != null
                            ? eventMember.getAddedIn().toString() : null)
                    .invitationDate(eventMember.getInvitationDate() != null
                            ? eventMember.getInvitationDate().toString() : null)
                    .invitationDisplayed(eventMember.isInvitationDisplayed())
                    .build();
            eventMemberDtoList.add(eventMemberDto);
        }

        return eventMemberDtoList;
    }
}