package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Post;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class PostDtoMapper implements Converter<PostDto, Post> {

    @Override
    public PostDto convert(Post from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return PostDto.builder()
                .postId(from.getPostId())
                .postAuthorId(from.getPostAuthor().getUserId())
                .postAuthor(from.getPostAuthor().getUserProfile().getFirstName()
                        + " " + from.getPostAuthor().getUserProfile().getLastName())
                .text(from.getText())
                .images(
                        from.getImages().stream()
                                .map(image -> ImageDto.builder()
                                        .filename(image.getFilename())
                                        .url("localhost:8080/api/images/" + image.getImageId())
                                        .type(image.getType())
                                        .build())
                                .collect(Collectors.toList())
                )
                .createdAt(from.getCreatedAt().format(formatter))
                .editedAt(from.getEditedAt() != null ? from.getEditedAt().format(formatter) : null)
                .isPublic(from.isPublic())
                .isEdited(from.isEdited())
                .likes(
                        from.getLikedPosts().stream()
                                .map(likedPost -> LikedPostDto.builder()
                                        .userId(likedPost.getLikedPostUser().getUserId())
                                        .name(likedPost.getLikedPostUser().getUserProfile().getFirstName()
                                                + " " + likedPost.getLikedPostUser().getUserProfile().getLastName())
                                        .date(likedPost.getDate().format(formatter))
                                        .build())
                                .collect(Collectors.toList())
                )
                .comments(
                        from.getComments().stream()
                                .map(comment -> CommentDto.builder()
                                        .commentId(comment.getCommentId())
                                        .text(comment.getText())
                                        .createdAt(comment.getCreatedAt().format(formatter))
                                        .editedAt(comment.getEditedAt() != null ? comment.getEditedAt().format(formatter) : null)
                                        .isEdited(comment.isEdited())
                                        .authorName(comment.getCommentAuthor().getUserProfile().getFirstName()
                                                + " " + comment.getCommentAuthor().getUserProfile().getLastName())
                                        .userLikes(
                                                comment.getLikes().stream()
                                                        .map(user -> LikedCommentDto.builder()
                                                                .userId(user.getUserId())
                                                                .name(user.getUserProfile().getFirstName() +
                                                                        " " + user.getUserProfile().getLastName())
                                                                .build())
                                                        .collect(Collectors.toList()))
                                        .build())
                                .collect(Collectors.toList())
                )
                .sharing(
                        from.getSharedBasePosts().stream()
                                .map(sharedPost -> SharedPostInfoDto.builder()
                                        .userId(sharedPost.getSharedPostUser().getUserId())
                                        .authorOfSharing(sharedPost.getSharedPostUser().getUserProfile().getFirstName() +
                                                " " + sharedPost.getSharedPostUser().getUserProfile().getLastName())
                                        .sharingText(sharedPost.getNewPost().getText())
                                        .isPublic(sharedPost.getNewPost().isPublic())
                                        .date(sharedPost.getDate().format(formatter))
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
