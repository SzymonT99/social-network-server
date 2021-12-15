package com.server.springboot.controller;

import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.service.EventService;
import com.server.springboot.service.PostCommentService;
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
    private final EventService eventService;
    private final PostCommentService postCommentService;

    @Autowired
    public UserApiController(PostService postService, EventService eventService, PostCommentService postCommentService) {
        this.postService = postService;
        this.eventService = eventService;
        this.postCommentService = postCommentService;
    }

}
