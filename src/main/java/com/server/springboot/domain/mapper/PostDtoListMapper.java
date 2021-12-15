package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostDtoListMapper implements Converter<List<PostDto>, List<Post>> {

    @Override
    public List<PostDto> convert(List<Post> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        List<PostDto> postsDto = new ArrayList<>();

        for (Post post : from) {
            UserProfile userProfile = post.getPostAuthor().getUserProfile();
            PostDto postDto = PostDto.builder()
                    .postId(post.getPostId())
                    .postAuthorId(post.getPostAuthor().getUserId())
                    .postAuthor(userProfile.getFirstName() + " " + userProfile.getLastName())
                    .text(post.getText())
                    .images(
                            post.getImages().stream()
                                    .map(image -> ImageDto.builder()
                                            .filename(image.getFilename())
                                            .url("localhost:8080/api/images/" + image.getImageId())
                                            .type(image.getType())
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .createdAt(post.getCreatedAt().format(formatter))
                    .editedAt(post.getEditedAt() != null ? post.getEditedAt().format(formatter) : null)
                    .isPublic(post.isPublic())
                    .isEdited(post.isEdited())
                    .likes(
                            post.getLikedPosts().stream()
                                    .map(likedPost -> LikedPostDto.builder()
                                            .userId(likedPost.getLikedPostUser().getUserId())
                                            .name(likedPost.getLikedPostUser().getUserProfile().getFirstName()
                                                    + " " + likedPost.getLikedPostUser().getUserProfile().getLastName())
                                            .date(likedPost.getDate().format(formatter))
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .comments(
                            post.getComments().stream()
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
                            post.getSharedBasePosts().stream()
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

            postsDto.add(postDto);
        }

        return postsDto;
    }
}
