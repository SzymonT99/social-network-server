package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostDtoMapper implements Converter<PostDto, Post> {

    private final Converter<List<ImageDto>, List<Image>> imageDtoListMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<CommentDto>, List<Comment>> commentDtoListMapper;
    private final Converter<LikedPostDto, LikedPost> likedPostDtoMapper;

    @Autowired
    public PostDtoMapper() {
        this.imageDtoListMapper = new ImageDtoListMapper();
        this.userDtoMapper = new UserDtoMapper();
        this.commentDtoListMapper = new CommentDtoListMapper();
        this.likedPostDtoMapper = new LikedPostDtoMapper();
    }

    @Override
    public PostDto convert(Post from) {
        return PostDto.builder()
                .postId(from.getPostId())
                .postAuthor(userDtoMapper.convert(from.getPostAuthor()))
                .text(from.getText())
                .images(from.getImages() != null ? imageDtoListMapper.convert(Lists.newArrayList(from.getImages())) : new ArrayList<>())
                .createdAt(from.getCreatedAt().toString())
                .editedAt(from.getEditedAt() != null ? from.getEditedAt().toString() : null)
                .isPublic(from.isPublic())
                .isCommentingBlocked(from.isCommentingBlocked())
                .isEdited(from.isEdited())
                .likes(from.getLikedPosts() != null ? from.getLikedPosts().stream().map(likedPostDtoMapper::convert).collect(Collectors.toList()) : new ArrayList<>())
                .comments(from.getComments() != null ? commentDtoListMapper.convert(Lists.newArrayList(from.getComments()).stream()
                        .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                        .collect(Collectors.toList())) : new ArrayList<>())
                .sharing(from.getSharedBasePosts() != null ?
                        from.getSharedBasePosts().stream()
                                .map(sharedPost -> SharedPostInfoDto.builder()
                                        .shardPostId(sharedPost.getSharedPostId())
                                        .authorOfSharing(userDtoMapper.convert(sharedPost.getSharedPostUser()))
                                        .sharingText(sharedPost.getNewPost().getText())
                                        .isPublic(sharedPost.getNewPost().isPublic())
                                        .date(sharedPost.getDate().toString())
                                        .build())
                                .collect(Collectors.toList()) : new ArrayList<>()
                )
                .build();
    }
}
