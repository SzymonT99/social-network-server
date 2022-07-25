package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.GroupThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupDetailsDtoMapper implements Converter<GroupDetailsDto, Group>{

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<PostDto>, List<Post>> postDtoListMapper;
    private final Converter<List<GroupRuleDto>, List<GroupRule>> groupRuleDtoListMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;

    @Autowired
    public GroupDetailsDtoMapper() {
        this.imageDtoMapper = new ImageDtoMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.postDtoListMapper = new PostDtoListMapper();
        this.groupRuleDtoListMapper = new GroupRuleDtoListMapper();
        this.interestDtoListMapper = new InterestDtoListMapper();
    }

    @Override
    public GroupDetailsDto convert(Group from) {

        List<Post> filteredPosts = from.getPosts().stream()
                .filter(el -> !el.isDeleted())
                .collect(Collectors.toList());

        return GroupDetailsDto.builder()
                .groupId(from.getGroupId())
                .name(from.getName())
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .description(from.getDescription())
                .createdAt(from.getCreatedAt().toString())
                .isPublic(from.isPublic())
                .groupCreator(userDtoMapper.convert(from.getGroupCreator()))
                .rules(groupRuleDtoListMapper.convert(Lists.newArrayList(from.getGroupRules().stream().
                        sorted(Comparator.comparing(GroupRule::getName))
                        .collect(Collectors.toList()))))
                .posts(postDtoListMapper.convert(filteredPosts))
                .interests(interestDtoListMapper.convert(Lists.newArrayList(from.getGroupInterests().stream().
                        sorted(Comparator.comparing(Interest::getName))
                        .collect(Collectors.toList()))))
                .build();
    }
}
