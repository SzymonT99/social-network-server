package com.server.springboot.controller;

import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationApiController.class);
    private final PostService postService;

    @Autowired
    public UserApiController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(value = "/users/{userId}/favourite-posts")
    public ResponseEntity<List<PostDto>> getUserFavouritePosts(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- Get all favourite post for user with id {}", userId);
        return new ResponseEntity<>(postService.findAllFavouritePostsByUserId(userId), HttpStatus.OK);
    }
}
