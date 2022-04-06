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
public class SharedPostDtoListMapper implements Converter<List<SharedPostDto>, List<SharedPost>> {

    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<LikedPostDto, LikedPost> likedPostDtoMapper;
    private final Converter<List<CommentDto>, List<Comment>> commentDtoListMapper;

    @Autowired
    public SharedPostDtoListMapper() {
        this.postDtoMapper = new PostDtoMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.commentDtoListMapper = new CommentDtoListMapper();
        this.likedPostDtoMapper = new LikedPostDtoMapper();
    }

    @Override
    public List<SharedPostDto> convert(List<SharedPost> from) {
        List<SharedPostDto> sharedPostDtoList = new ArrayList<>();


        from = from.stream()
                .sorted(Comparator.comparing(SharedPost::getDate).reversed())
                .collect(Collectors.toList());

        for (SharedPost sharedPost : from) {
            SharedPostDto sharedPostDto = SharedPostDto.builder()
                    .sharedPostId(sharedPost.getSharedPostId())
                    .sharingId(sharedPost.getNewPost().getPostId())
                    .authorOfSharing(userDtoMapper.convert(sharedPost.getSharedPostUser()))
                    .sharingText(sharedPost.getNewPost().getText())
                    .sharingDate(sharedPost.getDate().toString())
                    .isPublic(sharedPost.getNewPost().isPublic())
                    .isCommentingBlocked(sharedPost.getNewPost().isCommentingBlocked())
                    .sharingLikes(sharedPost.getNewPost().getLikedPosts() != null
                            ? sharedPost.getNewPost().getLikedPosts().stream().map(likedPostDtoMapper::convert).collect(Collectors.toList())
                            : new ArrayList<>())
                    .sharingComments(sharedPost.getNewPost().getComments() != null
                            ? commentDtoListMapper.convert(Lists.newArrayList(sharedPost.getNewPost().getComments()).stream()
                            .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                            .collect(Collectors.toList())) : new ArrayList<>())
                    .sharedPost(postDtoMapper.convert(sharedPost.getBasePost()))
                    .build();
            sharedPostDtoList.add(sharedPostDto);
        }

        return sharedPostDtoList;
    }
}