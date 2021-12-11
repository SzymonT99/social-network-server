package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.entity.Image;

import java.util.List;

public interface PostService {

    void addPost(RequestPostDto requestPostDto, List<Image> images);

}
