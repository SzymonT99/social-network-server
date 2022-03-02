package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupDtoListMapper implements Converter<List<GroupDto>, List<Group>> {

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public GroupDtoListMapper(Converter<ImageDto, Image> imageDtoMapper, Converter<UserDto, User> userDtoMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<GroupDto> convert(List<Group> from) {
        List<GroupDto> groupDtoList = new ArrayList<>();
        for (Group group : from) {
            GroupDto groupDto = GroupDto.builder()
                    .groupId(group.getGroupId())
                    .name(group.getName())
                    .image(group.getImage() != null ? imageDtoMapper.convert(group.getImage()) : null)
                    .description(group.getDescription())
                    .createdAt(group.getCreatedAt().toString())
                    .isPublic(group.isPublic())
                    .groupCreator(userDtoMapper.convert(group.getGroupCreator()))
                    .membersNumber(group.getGroupMembers().stream().count())
                    .postsNumber(group.getPosts().stream().count())
                    .build();

            groupDtoList.add(groupDto);
        }
        return groupDtoList;
    }
}
