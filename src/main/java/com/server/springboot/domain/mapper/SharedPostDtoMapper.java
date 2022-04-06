package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SharedPostDtoMapper implements Converter<SharedPostDto, SharedPost> {

    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<LikedPostDto, LikedPost> likedPostDtoMapper;
    private final Converter<List<CommentDto>, List<Comment>> commentDtoListMapper;

    @Autowired
    public SharedPostDtoMapper() {
        this.postDtoMapper = new PostDtoMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.commentDtoListMapper = new CommentDtoListMapper();
        this.likedPostDtoMapper = new LikedPostDtoMapper();
    }

    @Override
    public SharedPostDto convert(SharedPost from) {

        return SharedPostDto.builder()
                .sharedPostId(from.getSharedPostId())
                .sharingId(from.getNewPost().getPostId())
                .authorOfSharing(userDtoMapper.convert(from.getSharedPostUser()))
                .sharingText(from.getNewPost().getText())
                .sharingDate(from.getDate().toString())
                .isPublic(from.getNewPost().isPublic())
                .isCommentingBlocked(from.getNewPost().isCommentingBlocked())
                .sharingLikes(from.getNewPost().getLikedPosts() != null
                        ? from.getNewPost().getLikedPosts().stream().map(likedPostDtoMapper::convert).collect(Collectors.toList())
                        : new ArrayList<>())
                .sharingComments(from.getNewPost().getComments() != null
                        ? commentDtoListMapper.convert(Lists.newArrayList(from.getNewPost().getComments()).stream()
                        .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                        .collect(Collectors.toList())) : new ArrayList<>())
                .sharedPost(postDtoMapper.convert(from.getBasePost()))
                .build();
    }
}
