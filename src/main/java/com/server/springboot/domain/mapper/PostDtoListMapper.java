package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostDtoListMapper implements Converter<List<PostDto>, List<Post>> {

    private final Converter<List<ImageDto>, List<Image>> imageDtoListMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<CommentDto>, List<Comment>> commentDtoListMapper;
    private final Converter<LikedPostDto, LikedPost> likedPostDtoMapper;

    @Autowired
    public PostDtoListMapper(Converter<List<ImageDto>, List<Image>> imageDtoListMapper, Converter<UserDto, User> userDtoMapper,
                             Converter<List<CommentDto>, List<Comment>> commentDtoListMapper,
                             Converter<LikedPostDto, LikedPost> likedPostDtoMapper) {
        this.imageDtoListMapper = imageDtoListMapper;
        this.userDtoMapper = userDtoMapper;
        this.commentDtoListMapper = commentDtoListMapper;
        this.likedPostDtoMapper = likedPostDtoMapper;
    }

    @Override
    public List<PostDto> convert(List<Post> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        List<PostDto> postsDto = new ArrayList<>();

        for (Post post : from) {
            PostDto postDto = PostDto.builder()
                    .postId(post.getPostId())
                    .postAuthor(userDtoMapper.convert(post.getPostAuthor()))
                    .text(post.getText())
                    .images(imageDtoListMapper.convert(Lists.newArrayList(post.getImages())))
                    .createdAt(post.getCreatedAt().format(formatter))
                    .editedAt(post.getEditedAt() != null ? post.getEditedAt().format(formatter) : null)
                    .isPublic(post.isPublic())
                    .isCommentingBlocked(post.isCommentingBlocked())
                    .isEdited(post.isEdited())
                    .likes(post.getLikedPosts().stream().map(likedPostDtoMapper::convert).collect(Collectors.toList()))
                    .comments(commentDtoListMapper.convert(Lists.newArrayList(post.getComments())))
                    .sharing(
                            post.getSharedBasePosts().stream()
                                    .map(sharedPost -> SharedPostInfoDto.builder()
                                            .authorOfSharing(userDtoMapper.convert(sharedPost.getSharedPostUser()))
                                            .sharingText(sharedPost.getNewPost().getText())
                                            .isPublic(sharedPost.getNewPost().isPublic())
                                            .date(sharedPost.getDate().format(formatter))
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .build();

            postsDto.add(postDto);
        }

        return postsDto;
    }
}
