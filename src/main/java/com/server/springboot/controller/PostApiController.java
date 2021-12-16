package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import com.server.springboot.service.PostCommentService;
import com.server.springboot.service.PostService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class PostApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final PostService postService;
    private final PostCommentService postCommentService;

    @Autowired
    public PostApiController(PostService postService, PostCommentService postCommentService) {
        this.postService = postService;
        this.postCommentService = postCommentService;
    }

    @ApiOperation(value = "Create a post")
    @PostMapping(value = "/posts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createPost(@RequestPart(value = "images") List<MultipartFile> imageFiles,
                                        @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Create post");
        postService.addPost(requestPostDto, imageFiles);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing post by id")
    @PutMapping(value = "/posts/{postId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updatePost(@PathVariable(value = "postId") Long postId,
                                        @RequestPart(value = "images") List<MultipartFile> imageFiles,
                                        @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Update post with id: {}", postId);
        postService.editPost(postId, requestPostDto, imageFiles);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a post by id")
    @DeleteMapping(value = "/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable(value = "postId") Long postId) {
        Long authorId = 1L;
        LOGGER.info("---- Delete post with id: {}", postId);
        postService.deleteUserPostById(postId, authorId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a post by id with archiving")
    @DeleteMapping(value = "/posts/{postId}/archive")
    public ResponseEntity<?> deletePostWithArchiving(@PathVariable(value = "postId") Long postId) {
        Long authorId = 1L;
        LOGGER.info("---- Delete post by archiving with id: {}", postId);
        postService.deletePostByIdWithArchiving(postId, authorId, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all posts")
    @GetMapping(value = "/posts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        LOGGER.info("---- Get all posts");
        return new ResponseEntity<>(postService.findAllPosts(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get post by id")
    @GetMapping(value = "/posts/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable(value = "postId") Long postId) {
        LOGGER.info("---- Get post with id: {}", postId);
        return new ResponseEntity<>(postService.findPostById(postId), HttpStatus.OK);
    }

    @ApiOperation(value = "Like post by id")
    @PostMapping(value = "/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable(value = "postId") Long postId) {
        Long userId = 1L;
        LOGGER.info("---- User with id: {} likes post with id: {}", userId, postId);
        postService.likePost(postId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Dislike post by id")
    @DeleteMapping(value = "/posts/{postId}/liked")
    public ResponseEntity<?> deleteLikeFromPost(@PathVariable(value = "postId") Long postId) {
        Long userId = 1L;
        LOGGER.info("---- User with id: {} delete like from post with id: {}", userId, postId);
        postService.deleteLikeFromPost(postId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Share post by id")
    @PostMapping(value = "/posts/{basePostId}/share")
    public ResponseEntity<?> sharePost(@PathVariable(value = "basePostId") Long basePostId, @Valid @RequestBody RequestSharePostDto requestSharePostDto) {
        LOGGER.info("---- User with id: {} share post with id: {}", requestSharePostDto.getUserId(), basePostId);
        postService.sharePost(basePostId, requestSharePostDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete shared post by id")
    @DeleteMapping(value = "/posts/shared/{sharedPostId}")
    public ResponseEntity<?> deleteSharedPost(@PathVariable(value = "sharedPostId") Long sharedPostId) {
        Long userId = 1L;
        LOGGER.info("---- Deleted shared post with id: {}", sharedPostId);
        postService.deleteSharedPostById(sharedPostId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all shared post")
    @GetMapping(value = "/posts/shared")
    public ResponseEntity<List<SharedPostDto>> getAllSharedPosts() {
        LOGGER.info("---- Get all shared posts");
        return new ResponseEntity<>(postService.findAllSharedPosts(), HttpStatus.OK);
    }

    @ApiOperation(value = "Add post to favourite")
    @PostMapping(value = "/posts/{postId}/favourite")
    public ResponseEntity<?> addToFavouritePost(@PathVariable(value = "postId") Long postId) {
        Long userId = 1L;
        LOGGER.info("---- User with id: {} add post with id: {} to favourite posts", userId, postId);
        postService.addPostToFavourite(postId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get user's favourite posts")
    @GetMapping(value = "/posts/favourite")
    public ResponseEntity<List<PostDto>> getUserFavouritePosts(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Get all favourite post for user with id {}", userId);
        return new ResponseEntity<>(postService.findAllFavouritePostsByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Delete user's favourite post by id")
    @DeleteMapping(value = "/posts/{postId}/favourite")
    public ResponseEntity<?> deletePostFromFavourite(@PathVariable(value = "postId") Long postId) {
        Long userId = 1L;
        LOGGER.info("---- User with id: {} delete post with id: {} from favourite posts", userId, postId);
        postService.deletePostFromFavourite(postId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Create comment on the post by id")
    @PostMapping(value = "/posts/{postId}/comments")
    public ResponseEntity<?> addCommentToPost(@PathVariable(value = "postId") Long postId, @Valid @RequestBody RequestCommentDto requestCommentDto) {
        LOGGER.info("---- User with id: {} add comment to post with id: {}", requestCommentDto.getUserId(), postId);
        postCommentService.addComment(postId, requestCommentDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing post comment by id")
    @PutMapping(value = "/posts/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable(value = "commentId") Long commentId, @Valid @RequestBody RequestCommentDto requestCommentDto) {
        LOGGER.info("---- User with id: {} edit post comment with id: {}", requestCommentDto.getUserId(), commentId);
        postCommentService.editCommentById(commentId, requestCommentDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete post comment by id")
    @DeleteMapping(value = "/posts/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable(value = "commentId") Long commentId) {
        Long authorId = 1L;
        LOGGER.info("---- User with id: {} delete post comment with id: {}", authorId, commentId);
        postCommentService.deleteCommentById(commentId, authorId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Like post comment by id")
    @PostMapping(value = "/posts/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable(value = "commentId") Long commentId) {
        Long userId = 1L;
        LOGGER.info("---- User with id: {} like post comment with id: {}", userId, commentId);
        postCommentService.likeCommentById(commentId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Dislike post comment by id")
    @DeleteMapping(value = "/posts/comments/{commentId}/liked")
    public ResponseEntity<?> dislikeComment(@PathVariable(value = "commentId") Long commentId) {
        Long userId = 1L;
        LOGGER.info("---- User with id: {} like post comment with id: {}", userId, commentId);
        postCommentService.dislikeCommentById(commentId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}