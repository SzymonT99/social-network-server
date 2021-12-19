package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.CommentedPostDto;
import com.server.springboot.domain.dto.response.ImageDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentedPostDtoListMapper implements Converter <List<CommentedPostDto>, List<Comment>> {

    private final Converter<ImageDto, Image> imageDtoMapper;

    @Autowired
    public CommentedPostDtoListMapper(Converter<ImageDto, Image> imageDtoMapper) {
        this.imageDtoMapper = imageDtoMapper;
    }

    @Override
    public List<CommentedPostDto> convert(List<Comment> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        List<CommentedPostDto> commentedPostDtoList = new ArrayList<>();

        for (Comment comment : from) {
            CommentedPostDto commentedPostDto = CommentedPostDto.builder()
                    .commentId(comment.getCommentId())
                    .commentAuthor(comment.getCommentAuthor().getUserProfile().getFirstName()
                            + " " + comment.getCommentAuthor().getUserProfile().getLastName())
                    .commentText(comment.getText())
                    .postId(comment.getCommentedPost().getPostId())
                    .postAuthorId(comment.getCommentedPost().getPostAuthor().getUserId())
                    .postAuthor(comment.getCommentedPost().getPostAuthor().getUserProfile().getFirstName()
                            + " " + comment.getCommentedPost().getPostAuthor().getUserProfile().getLastName())
                    .postText(comment.getCommentedPost().getText())
                    .postImages(comment.getCommentedPost().getImages().stream()
                            .map(imageDtoMapper::convert)
                            .collect(Collectors.toList()))
                    .createdAt(comment.getCreatedAt().format(formatter))
                    .editedAt(comment.getEditedAt() != null ? comment.getEditedAt().format(formatter) : null)
                    .isEdited(comment.isEdited())
                    .build();

            commentedPostDtoList.add(commentedPostDto);
        }
        return commentedPostDtoList;
    }

}
