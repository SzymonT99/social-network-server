package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestPostDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {



    void addPost(RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    void editPost(Long postId, RequestPostDto requestPostDto, List<MultipartFile> imageFiles);

    void deletePostById(Long postId);

    void deletePostByIdWithArchiving(Long postId);
}
