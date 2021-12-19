package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class GroupDtoListMapper implements Converter<List<GroupDto>, List<Group>> {

    private final Converter<ImageDto, Image> imageDtoMapper;

    @Autowired
    public GroupDtoListMapper(Converter<ImageDto, Image> imageDtoMapper) {
        this.imageDtoMapper = imageDtoMapper;
    }


    @Override
    public List<GroupDto> convert(List<Group> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        List<GroupDto> groupDtoList = new ArrayList<>();
        for (Group group : from) {
            GroupDto groupDto = GroupDto.builder()
                    .groupId(group.getGroupId())
                    .name(group.getName())
                    .image(imageDtoMapper.convert(group.getImage()))
                    .description(group.getDescription())
                    .createdAt(group.getCreatedAt().format(formatter))
                    .build();

            groupDtoList.add(groupDto);
        }
        return groupDtoList;
    }
}
