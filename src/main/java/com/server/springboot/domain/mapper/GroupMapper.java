package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestGroupDto;
import com.server.springboot.domain.entity.*;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
                .groupRules(new HashSet<>())
                .groupMembers(new HashSet<>())
                .groupThreads(new HashSet<>())
                .posts(new HashSet<>())
                .groupInterests(new HashSet<>())
                .build();
    }
}