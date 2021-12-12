package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.service.FileService;
import com.server.springboot.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class PostApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final PostService postService;

    @Autowired
    public PostApiController(PostService postService) {
        this.postService = postService;
    }

    // tworzenie posta ze zdjeciami
    @PostMapping(value = "/posts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createPost(@RequestPart(value = "images") List<MultipartFile> imageFiles,
                                        @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Create post");
        postService.addPost(requestPostDto, imageFiles);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // aktualizacja posta
    @PutMapping(value = "/posts/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updatePost(@PathVariable(value = "id") Long postId,
                                        @RequestPart(value = "images") List<MultipartFile> imageFiles,
                                        @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Update post with id: {}", postId);
        postService.editPost(postId, requestPostDto, imageFiles);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // usuwanie posta
    @DeleteMapping(value = "/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable(value = "id") Long postId) {
        LOGGER.info("---- Delete post with id: {}", postId);
        postService.deletePostById(postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // usuwanie posta z archiwizacją
    @DeleteMapping(value = "/posts")
    public ResponseEntity<?> deletePostWithArchiving(@RequestParam(value = "id") Long postId, @RequestParam(value = "archive") @NotNull boolean archive) {
        LOGGER.info("---- Delete post by archiving with id: {}", postId);
        postService.deletePostByIdWithArchiving(postId, archive);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/posts")
    public ResponseEntity<List<PostDto>> getPosts() {
        return new ResponseEntity<>(postService.findAllPosts(), HttpStatus.OK);
    }

    @GetMapping(value = "/posts/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable(value = "id") Long postId) {
        return new ResponseEntity<>(postService.findPostById(postId), HttpStatus.OK);
    }
}
