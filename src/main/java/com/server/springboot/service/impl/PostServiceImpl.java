package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.PostRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final Converter<Post, RequestPostDto> postMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    @Autowired
    public PostServiceImpl(Converter<Post, RequestPostDto> postMapper, UserRepository userRepository, PostRepository postRepository) {
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void addPost(RequestPostDto requestPostDto, List<Image> images) {
        User author = userRepository.findById(requestPostDto.getUserId())
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requestPostDto.getUserId()));
        Post addedPost = postMapper.convert(requestPostDto);
        addedPost.setPostAuthor(author);
        addedPost.setImages(images);
        postRepository.save(addedPost);
    }
}