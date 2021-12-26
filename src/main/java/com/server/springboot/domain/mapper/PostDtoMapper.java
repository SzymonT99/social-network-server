package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostDtoMapper implements Converter<PostDto, Post> {

    private final Converter<List<ImageDto>, List<Image>> imageDtoListMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<CommentDto>, List<Comment>> commentDtoListMapper;

    @Autowired
    public PostDtoMapper(Converter<List<ImageDto>, List<Image>> imageDtoListMapper, Converter<UserDto, User> userDtoMapper,
                         Converter<List<CommentDto>, List<Comment>> commentDtoListMapper) {
        this.imageDtoListMapper = imageDtoListMapper;
        this.userDtoMapper = userDtoMapper;
        this.commentDtoListMapper = commentDtoListMapper;
    }

    @Override
    public PostDto convert(Post from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return PostDto.builder()
                .postId(from.getPostId())
                .postAuthor(userDtoMapper.convert(from.getPostAuthor()))
                .text(from.getText())
                .images(imageDtoListMapper.convert(Lists.newArrayList(from.getImages())))
                .createdAt(from.getCreatedAt().format(formatter))
                .editedAt(from.getEditedAt() != null ? from.getEditedAt().format(formatter) : null)
                .isPublic(from.isPublic())
                .isCommentingBlocked(from.isCommentingBlocked())
                .isEdited(from.isEdited())
                .likes(
                        from.getLikedPosts().stream()
                                .map(likedPost -> LikedPostDto.builder()
                                        .likedUser(userDtoMapper.convert(likedPost.getLikedPostUser()))
                                        .date(likedPost.getDate().format(formatter))
                                        .build())
                                .collect(Collectors.toList())
                )
                .comments(commentDtoListMapper.convert(Lists.newArrayList(from.getComments())))
                .sharing(
                        from.getSharedBasePosts().stream()
                                .map(sharedPost -> SharedPostInfoDto.builder()
                                        .authorOfSharing(userDtoMapper.convert(sharedPost.getSharedPostUser()))
                                        .sharingText(sharedPost.getNewPost().getText())
                                        .isPublic(sharedPost.getNewPost().isPublic())
                                        .date(sharedPost.getDate().format(formatter))
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
