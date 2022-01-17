package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    List<PostDto> findAllPublicPosts();

    PostDto addPost(RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    @Transactional
    PostDto editPost(Long postId, RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    void deleteUserPostById(Long postId, boolean archive);

    PostDto findPostById(Long postId);

    void likePost(Long postId);

    void deleteLikeFromPost(Long postId);

    void sharePost(Long basePostId, RequestSharePostDto requestSharePostDto);

    void deleteSharedPostById(Long sharedPostId);

    List<SharedPostDto> findAllSharedPosts();

    void addPostToFavourite(Long postId);

    void deletePostFromFavourite(Long postId);

    List<PostDto> findAllFavouritePostsByUserId(Long userId);

    List<PostDto> findPostsByUserId(Long userId);
}
