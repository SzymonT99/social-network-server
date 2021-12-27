package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ChangeProfilePhotoDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ChangeProfilePhotoDtoMapper implements Converter<ChangeProfilePhotoDto, Image> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public ChangeProfilePhotoDtoMapper(Converter<UserDto, User> userDtoMapper) {
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public ChangeProfilePhotoDto convert(Image from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return ChangeProfilePhotoDto.builder()
                .user(userDtoMapper.convert(from.getUserProfile().getUser()))
                .filename(from.getFilename())
                .url("localhost:8080/api/images/" + from.getImageId())
                .type(from.getType())
                .caption(from.getCaption())
                .addedIn(from.getAddedIn().format(formatter))
                .build();
    }
}

