package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupDtoMapper implements Converter<GroupDto, Group> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public GroupDtoMapper(Converter<ImageDto, Image> imageDtoMapper, Converter<UserDto, User> userDtoMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public GroupDto convert(Group from) {
        return GroupDto.builder()
                .groupId(from.getGroupId())
                .name(from.getName())
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .description(from.getDescription())
                .createdAt(from.getCreatedAt().toString())
                .isPublic(from.isPublic())
                .groupCreator(userDtoMapper.convert(from.getGroupCreator()))
                .membersNumber(from.getGroupMembers().stream().count())
                .postsNumber(from.getPosts().stream().count())
                .build();
    }
}

