package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Image;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageDtoListMapper implements Converter<List<ImageDto>, List<Image>> {

    @Override
    public List<ImageDto> convert(List<Image> from) {

        from = from.stream()
                .sorted(Comparator.comparing(Image::getAddedIn).reversed())
                .collect(Collectors.toList());

        List<ImageDto> imageDtoList = new ArrayList<>();
        for (Image image: from) {
            ImageDto imageDto = ImageDto.builder()
                    .filename(image.getFilename())
                    .url("http://localhost:8080/api/images/" + image.getImageId())
                    .type(image.getType())
                    .build();
            imageDtoList.add(imageDto);
        }
        return imageDtoList;
    }
}
