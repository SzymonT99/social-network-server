package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.response.PostDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    List<PostDto> findAllPosts();

    void addPost(RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    void editPost(Long postId, RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    @Transactional
    void deletePostById(Long postId);

    void deletePostByIdWithArchiving(Long postId, boolean archive);

    PostDto findPostById(Long postId);
}
