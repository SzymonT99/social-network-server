package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.entity.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageDtoMapper implements Converter<ImageDto, Image>{

    @Override
    public ImageDto convert(Image from) {
        return ImageDto.builder()
                .filename(from.getFilename())
                .url("localhost:8080/api/images/" + from.getImageId())
                .type(from.getType())
                .build();
    }
}
