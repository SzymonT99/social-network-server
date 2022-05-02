package com.server.springboot.domain.mapper;

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
public class CommentedPostDtoListMapper implements Converter<List<CommentedPostDto>, List<Comment>> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<SharedPostDto, SharedPost> sharedPostDtoMapper;

    @Autowired
    public CommentedPostDtoListMapper() {
        this.userDtoMapper = new UserDtoMapper();
        this.postDtoMapper = new PostDtoMapper();
        this.sharedPostDtoMapper = new SharedPostDtoMapper();
    }

    @Override
    public List<CommentedPostDto> convert(List<Comment> from) {
        List<CommentedPostDto> commentedPostDtoList = new ArrayList<>();

        from = from.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());

        for (Comment comment : from) {
            CommentedPostDto commentedPostDto = CommentedPostDto.builder()
                    .commentId(comment.getCommentId())
                    .commentAuthor(userDtoMapper.convert(comment.getCommentAuthor()))
                    .commentText(comment.getText())
                    .commentedPost(comment.getCommentedPost().getSharedNewPost() == null
                            ? postDtoMapper.convert(comment.getCommentedPost())
                            : null)
                    .commentedSharedPost(comment.getCommentedPost().getSharedNewPost() != null
                            ? sharedPostDtoMapper.convert(comment.getCommentedPost().getSharedNewPost())
                            : null)
                    .createdAt(comment.getCreatedAt().toString())
                    .editedAt(comment.getEditedAt() != null ? comment.getEditedAt().toString() : null)
                    .isEdited(comment.isEdited())
                    .build();

            commentedPostDtoList.add(commentedPostDto);
        }
        return commentedPostDtoList;
    }

}
