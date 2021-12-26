package com.server.springboot.domain.mapper;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.CommentedPostDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommentedPostDtoListMapper implements Converter<List<CommentedPostDto>, List<Comment>> {

    private final Converter<UserDto, User> userDtoMapper;
    private final Converter<List<ImageDto>, List<Image>> imageDtoListMapper;

    @Autowired
    public CommentedPostDtoListMapper(Converter<UserDto, User> userDtoMapper,
                                      Converter<List<ImageDto>, List<Image>> imageDtoListMapper) {
        this.userDtoMapper = userDtoMapper;
        this.imageDtoListMapper = imageDtoListMapper;
    }

    @Override
    public List<CommentedPostDto> convert(List<Comment> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        List<CommentedPostDto> commentedPostDtoList = new ArrayList<>();

        for (Comment comment : from) {
            CommentedPostDto commentedPostDto = CommentedPostDto.builder()
                    .commentId(comment.getCommentId())
                    .commentAuthor(userDtoMapper.convert(comment.getCommentAuthor()))
                    .commentText(comment.getText())
                    .postId(comment.getCommentedPost().getPostId())
                    .postAuthor(userDtoMapper.convert(comment.getCommentedPost().getPostAuthor()))
                    .postText(comment.getCommentedPost().getText())
                    .postImages(imageDtoListMapper.convert(Lists.newArrayList(comment.getCommentedPost().getImages())))
                    .createdAt(comment.getCreatedAt().format(formatter))
                    .editedAt(comment.getEditedAt() != null ? comment.getEditedAt().format(formatter) : null)
                    .isEdited(comment.isEdited())
                    .build();

            commentedPostDtoList.add(commentedPostDto);
        }
        return commentedPostDtoList;
    }

}
