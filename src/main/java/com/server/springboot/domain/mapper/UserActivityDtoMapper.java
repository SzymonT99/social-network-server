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
    private final Converter<List<GroupDto>, List<Group>> groupDtoListMapper;

    @Autowired
    public UserActivityDtoMapper() {
        this.postDtoListMapper = new PostDtoListMapper();
        this.commentedPostDtoListMapper = new CommentedPostDtoListMapper();
        this.sharedPostDtoListMapper = new SharedPostDtoListMapper();
        this.sharedEventDtoListMapper = new SharedEventDtoListMapper();
        this.groupDtoListMapper = new GroupDtoListMapper();
    }


    @Override
    public UserActivityDto convert(User from) {

        List<Post> sharingList = from.getSharedPosts().stream()
                .map(SharedPost::getNewPost)
                .collect(Collectors.toList());

        List<Post> createdPosts = from.getPosts().stream()
                .filter(el -> !sharingList.contains(el) && !el.isDeleted())
                .collect(Collectors.toList());

        List<Post> likedPosts = from.getLikedPosts().stream()
                .map(LikedPost::getPost)
                .filter(el -> !sharingList.contains(el) && !el.isDeleted())
                .collect(Collectors.toList());

        List<Comment> comments = from.getComments().stream()
                .filter(el -> !el.getCommentedPost().isDeleted())
                .collect(Collectors.toList());

        return UserActivityDto.builder()
                .userProfileId(from.getUserProfile().getUserProfileId())
                .createdPosts(postDtoListMapper.convert(createdPosts))
                .likes(postDtoListMapper.convert(likedPosts))
                .comments(commentedPostDtoListMapper.convert(comments))
                .sharedPosts(sharedPostDtoListMapper.convert(Lists.newArrayList(from.getSharedPosts())))
                .sharedEvents(sharedEventDtoListMapper.convert(Lists.newArrayList(from.getSharedEvents())))
                .groups(groupDtoListMapper.convert(Lists.newArrayList(from.getMemberOfGroups().stream()
                        .map(GroupMember::getGroup)
                        .collect(Collectors.toList()))))
                .build();
    }
}
