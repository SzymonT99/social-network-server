package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    List<PostDto> findAllPosts();

    void addPost(RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    void editPost(Long postId, RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    void deletePostById(Long postId);

    void deletePostByIdWithArchiving(Long postId, boolean archive);

    PostDto findPostById(Long postId);

    void likePost(Long postId, Long userId);

    void deleteLikeFromPost(Long postId, Long userId);

    void sharePost(Long basePostId, RequestSharePostDto requestSharePostDto);

    void deleteSharedPostById(Long sharedPostId, Long userId);

    List<SharedPostDto> findAllSharedPosts();
}
