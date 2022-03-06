package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestGroupDto;
import com.server.springboot.domain.entity.Group;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GroupMapper implements Converter<Group, RequestGroupDto> {

    @Override
    public Group convert(RequestGroupDto from) {
        return Group.builder()
                .description(from.getDescription())
                .name(from.getName())
                .createdAt(LocalDateTime.now())
                .isPublic(Boolean.parseBoolean(from.getIsPublic()))
                .isDeleted(false)
                .build();
    }

}
