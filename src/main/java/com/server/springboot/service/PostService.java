package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.PostsPageDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    PostsPageDto findAllPublicPosts(Integer page, Integer size);

    PostDto addPost(RequestPostDto requestPostDto, List<MultipartFile> imageFiles, Long groupId);

    @Transactional
    PostDto editPost(Long postId, RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    void deletePostById(Long postId, boolean archive);

    PostDto findPostById(Long postId);

    void likePost(Long postId);

    void deleteLikeFromPost(Long postId);

    SharedPostDto sharePost(Long basePostId, RequestSharePostDto requestSharePostDto);

    void deleteSharedPostById(Long sharedPostId);

    void addPostToFavourite(Long postId);

    void deletePostFromFavourite(Long postId);

    List<PostDto> findAllFavouritePostsByUserId(Long userId);

    void setPostCommentsAvailability(Long postId, boolean isBlocked);

    void setPostAccess(Long postId, boolean isPublic);
}
