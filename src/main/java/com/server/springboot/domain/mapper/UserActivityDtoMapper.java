package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserActivityDtoMapper implements Converter<UserActivityDto, User> {

    private final Converter<List<PostDto>, List<Post>> postDtoListMapper;
    private final Converter<List<CommentedPostDto>, List<Comment>> commentedPostDtoListMapper;
    private final Converter<List<SharedPostDto>, List<SharedPost>> sharedPostDtoListMapper;
    private final Converter<List<SharedEventDto>, List<SharedEvent>> sharedEventDtoListMapper;
    private final Converter<List<FriendDto>, List<Friend>> friendDtoMapper;
    private final Converter<List<GroupDto>, List<Group>> groupDtoListMapper;

    @Autowired
    public UserActivityDtoMapper(Converter<List<PostDto>, List<Post>> postDtoListMapper,
                                 Converter<List<CommentedPostDto>, List<Comment>> commentedPostDtoListMapper,
                                 Converter<List<SharedPostDto>, List<SharedPost>> sharedPostDtoListMapper,
                                 Converter<List<SharedEventDto>, List<SharedEvent>> sharedEventDtoListMapper,
                                 Converter<List<FriendDto>, List<Friend>> friendDtoMapper,
                                 Converter<List<GroupDto>, List<Group>> groupDtoListMapper) {
        this.postDtoListMapper = postDtoListMapper;
        this.commentedPostDtoListMapper = commentedPostDtoListMapper;
        this.sharedPostDtoListMapper = sharedPostDtoListMapper;
        this.sharedEventDtoListMapper = sharedEventDtoListMapper;
        this.friendDtoMapper = friendDtoMapper;
        this.groupDtoListMapper = groupDtoListMapper;
    }


    @Override
    public UserActivityDto convert(User from) {
        return UserActivityDto.builder()
                .userProfileId(from.getUserProfile().getUserProfileId())
                .createdPosts(postDtoListMapper.convert(Lists.newArrayList(from.getPosts())))
                .likes(postDtoListMapper.convert(Lists.newArrayList(from.getLikedPosts().stream()
                        .map(LikedPost::getPost)
                        .collect(Collectors.toList()))))
                .comments(commentedPostDtoListMapper.convert(Lists.newArrayList(from.getComments())))
                .sharedPosts(sharedPostDtoListMapper.convert(Lists.newArrayList(from.getSharedPosts())))
                .sharedEvents(sharedEventDtoListMapper.convert(Lists.newArrayList(from.getSharedEvents())))
                .friends(friendDtoMapper.convert(Lists.newArrayList(from.getUserFriends())))
                .groups(groupDtoListMapper.convert(Lists.newArrayList(from.getMemberOfGroups().stream()
                        .map(GroupMember::getGroup)
                        .collect(Collectors.toList()))))
                .build();
    }
}
