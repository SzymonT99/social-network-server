package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestThreadDto;
import com.server.springboot.domain.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ThreadMapper implements Converter<GroupThread, RequestThreadDto> {

    @Override
    public GroupThread convert(RequestThreadDto from) {
        return GroupThread.builder()
                .title(from.getTitle())
                .content(from.getContent())
                .createdAt(LocalDateTime.now())
                .build();
    }
}