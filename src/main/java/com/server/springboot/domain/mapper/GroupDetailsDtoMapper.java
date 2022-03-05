package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.GroupThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupDetailsDtoMapper implements Converter<GroupDetailsDto, Group>{

    private final Converter<ImageDto, Image> imageDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<PostDto>, List<Post>> postDtoListMapper;
    private final Converter<List<GroupRuleDto>, List<GroupRule>> groupRuleDtoListMapper;
    private final Converter<List<GroupThreadDto>, List<GroupThread>> groupThreadDtoListMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;

    @Autowired
    public GroupDetailsDtoMapper(Converter<ImageDto, Image> imageDtoMapper,
                                 Converter<UserDto, User> userDtoMapper,
                                 Converter<List<PostDto>, List<Post>> postDtoListMapper,
                                 Converter<List<GroupRuleDto>, List<GroupRule>> groupRuleDtoListMapper,
                                 Converter<List<GroupThreadDto>, List<GroupThread>> groupThreadDtoListMapper,
                                 Converter<List<InterestDto>, List<Interest>> interestDtoListMapper) {
        this.imageDtoMapper = imageDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.postDtoListMapper = postDtoListMapper;
        this.groupRuleDtoListMapper = groupRuleDtoListMapper;
        this.groupThreadDtoListMapper = groupThreadDtoListMapper;
        this.interestDtoListMapper = interestDtoListMapper;
    }

    @Override
    public GroupDetailsDto convert(Group from) {
        return GroupDetailsDto.builder()
                .groupId(from.getGroupId())
                .name(from.getName())
                .image(from.getImage() != null ? imageDtoMapper.convert(from.getImage()) : null)
                .description(from.getDescription())
                .createdAt(from.getCreatedAt().toString())
                .isPublic(from.isPublic())
                .groupCreator(userDtoMapper.convert(from.getGroupCreator()))
                .rules(groupRuleDtoListMapper.convert(Lists.newArrayList(from.getGroupRules())))
                .posts(postDtoListMapper.convert(Lists.newArrayList(from.getPosts())))
                .threads(groupThreadDtoListMapper.convert(Lists.newArrayList(from.getGroupThreads())))
                .interests(interestDtoListMapper.convert(Lists.newArrayList(from.getGroupInterests())))
                .build();
    }
}
