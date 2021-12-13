package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import com.server.springboot.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping(value = "/posts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createPost(@RequestPart(value = "images") List<MultipartFile> imageFiles,
                                        @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Create post");
        postService.addPost(requestPostDto, imageFiles);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/posts/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updatePost(@PathVariable(value = "id") Long postId,
                                        @RequestPart(value = "images") List<MultipartFile> imageFiles,
                                        @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Update post with id: {}", postId);
        postService.editPost(postId, requestPostDto, imageFiles);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable(value = "id") Long postId) {
        LOGGER.info("---- Delete post with id: {}", postId);
        postService.deletePostById(postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/posts")
    public ResponseEntity<?> deletePostWithArchiving(@RequestParam(value = "id") Long postId, @RequestParam(value = "archive") @NotNull boolean archive) {
        LOGGER.info("---- Delete post by archiving with id: {}", postId);
        postService.deletePostByIdWithArchiving(postId, archive);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/posts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        LOGGER.info("---- Get all posts");
        return new ResponseEntity<>(postService.findAllPosts(), HttpStatus.OK);
    }

    @GetMapping(value = "/posts/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable(value = "id") Long postId) {
        LOGGER.info("---- Get post with id: {}", postId);
        return new ResponseEntity<>(postService.findPostById(postId), HttpStatus.OK);
    }

    @PostMapping(value = "/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable(value = "postId") Long postId, @RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- User with id: {} likes post with id: {}", userId, postId);
        postService.likePost(postId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/posts/{postId}/like")
    public ResponseEntity<?> deleteLikeFromPost(@PathVariable(value = "postId") Long postId, @RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- User with id: {} likes post with id: {}", userId, postId);
        postService.deleteLikeFromPost(postId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/posts/{basePostId}/share")
    public ResponseEntity<?> sharePost(@PathVariable(value = "basePostId") Long basePostId, @Valid @RequestBody RequestSharePostDto requestSharePostDto) {
        LOGGER.info("---- User with id: {} share post with id: {}", requestSharePostDto.getUserId(), basePostId);
        postService.sharePost(basePostId, requestSharePostDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/shared-posts")
    public ResponseEntity<?> deleteSharedPost(@RequestParam(value = "sharedPostId") Long sharedPostId, @RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Deleted shared post with id: {}", sharedPostId);
        postService.deleteSharedPostById(sharedPostId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/shared-posts")
    public ResponseEntity<List<SharedPostDto>> getAllSharedPosts() {
        LOGGER.info("---- Get all shared posts");
        return new ResponseEntity<>(postService.findAllSharedPosts(), HttpStatus.OK);
    }
}
