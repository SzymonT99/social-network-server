package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ProfilePhotoDto;
import com.server.springboot.domain.entity.Image;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ProfilePhotoDtoMapper implements Converter<ProfilePhotoDto, Image> {
    @Override
    public ProfilePhotoDto convert(Image from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return ProfilePhotoDto.builder()
                .filename(from.getFilename())
                .url("localhost:8080/api/images/" + from.getImageId())
                .type(from.getType())
                .caption(from.getCaption())
                .addedIn(from.getAddedIn().format(formatter))
                .build();
    }
}