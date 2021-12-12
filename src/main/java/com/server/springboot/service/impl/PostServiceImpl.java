package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.ImageRepository;
import com.server.springboot.domain.repository.PostRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.service.FileService;
import com.server.springboot.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PostServiceImpl implements PostService {

    private final Converter<Post, RequestPostDto> postMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;


    @Autowired
    public PostServiceImpl(Converter<Post, RequestPostDto> postMapper, UserRepository userRepository,
                           PostRepository postRepository, ImageRepository imageRepository, FileService fileService) {
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.imageRepository = imageRepository;
        this.fileService = fileService;
    }

    @Override
    public void addPost(RequestPostDto requestPostDto, List<MultipartFile> imageFiles) {
        Set<Image> postImages = fileService.storageImages(imageFiles, requestPostDto.getUserId());
        User author = userRepository.findById(requestPostDto.getUserId())
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requestPostDto.getUserId()));
        Post addedPost = postMapper.convert(requestPostDto);
        addedPost.setPostAuthor(author);
        addedPost.setImages(postImages);
        postRepository.save(addedPost);
    }

    @Override
    public void editPost(Long postId, RequestPostDto requestPostDto, List<MultipartFile> imageFiles) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        Set<Image> lastImages =  new HashSet<>(post.getImages());
        post.removeImages();    // Usuwanie zdjęć dodanych przed edycją

        Set<Image> updatedImages = fileService.storageImages(imageFiles, requestPostDto.getUserId());
        post.setImages(updatedImages);
        post.setText(requestPostDto.getText());
        post.setPublic(Boolean.parseBoolean(requestPostDto.getIsPublic()));
        post.setEditedAt(LocalDateTime.now());
        post.setEdited(true);
        postRepository.save(post);
        imageRepository.deleteAll(lastImages);
        System.out.println("lastimages: " + lastImages.size());
    }

    @Override
    public void deletePostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        List<Image> lastImages = new ArrayList<>(post.getImages());
        postRepository.deleteByPostId(postId);
        imageRepository.deleteAll(lastImages);
    }

    @Override
    public void deletePostByIdWithArchiving(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        post.setDeleted(true);
        postRepository.save(post);
    }
}
