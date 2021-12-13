package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.key.UserPostKey;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ForbiddenException;
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
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final Converter<Post, RequestPostDto> postMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SharedPostRepository sharedPostRepository;
    private final LikedPostRepository likedPostRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;
    private final Converter<List<PostDto>, List<Post>> postDtoListMapper;
    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<Post, RequestSharePostDto> sharedPostMapper;
    private final Converter<List<SharedPostDto>, List<SharedPost>> sharedPostDtoListMapper;


    @Autowired
    public PostServiceImpl(Converter<Post, RequestPostDto> postMapper, UserRepository userRepository,
                           PostRepository postRepository, SharedPostRepository sharedPostRepository,
                           LikedPostRepository likedPostRepository, ImageRepository imageRepository,
                           FileService fileService, Converter<List<PostDto>, List<Post>> postDtoListMapper,
                           Converter<PostDto, Post> postDtoMapper, Converter<Post, RequestSharePostDto> sharedPostMapper,
                           Converter<List<SharedPostDto>, List<SharedPost>> sharedPostDtoListMapper) {
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.sharedPostRepository = sharedPostRepository;
        this.likedPostRepository = likedPostRepository;
        this.imageRepository = imageRepository;
        this.fileService = fileService;
        this.postDtoListMapper = postDtoListMapper;
        this.postDtoMapper = postDtoMapper;
        this.sharedPostMapper = sharedPostMapper;
        this.sharedPostDtoListMapper = sharedPostDtoListMapper;
    }

    @Override
    public List<PostDto> findAllPosts() {
        List<Post> posts = postRepository.findByIsDeletedOrderByCreatedAtDesc(false);

        List<Post> postWithShares = sharedPostRepository.findAll().stream()
                .map(SharedPost::getNewPost)
                .collect(Collectors.toList());      // ignorowanie postów które są udostępnieniem
        posts.removeAll(postWithShares);

        return postDtoListMapper.convert(posts);
    }

    @Override
    public PostDto findPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (post.isDeleted()) {
            throw new ForbiddenException("Post with id: " + postId + " is archived");
        }
        return postDtoMapper.convert(post);
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
        if (!post.getPostAuthor().getUserId().equals(requestPostDto.getUserId())) {
            throw new ForbiddenException("Invalid post author id - post editing access forbidden");
        }

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
    }

    @Override
    public void deletePostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        List<Image> lastImages = new ArrayList<>(post.getImages());
        sharedPostRepository.deleteAll(post.getSharedBasePosts());
        postRepository.deleteByPostId(postId);
        imageRepository.deleteAll(lastImages);
    }

    @Override
    public void deletePostByIdWithArchiving(Long postId, boolean archive) {
        if (archive) {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
            post.setDeleted(true);
            sharedPostRepository.deleteAll(post.getSharedBasePosts());
            postRepository.save(post);
        } else {
            deletePostById(postId);
        }
    }

    @Override
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        User likedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        LikedPost newLikedPost = LikedPost.builder()
                .id(UserPostKey.builder().postId(postId).userId(userId).build())
                .post(post)
                .likedPostUser(likedUser)
                .date(LocalDateTime.now())
                .build();
        Set<LikedPost> postLikes = post.getLikedPosts();
        postLikes.add(newLikedPost);
        post.setLikedPosts(postLikes);
        postRepository.save(post);
    }

    @Override
    public void deleteLikeFromPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (!post.getPostAuthor().getUserId().equals(userId)) {
            throw new ForbiddenException("Invalid post author id - post deleting access forbidden");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        likedPostRepository.deleteByPostAndLikedPostUser(post, user);
    }

    @Override
    public void sharePost(Long basePostId, RequestSharePostDto requestSharePostDto) {
        User user = userRepository.findById(requestSharePostDto.getUserId())
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requestSharePostDto.getUserId()));
        Post basePost = postRepository.findById(basePostId)
                .orElseThrow(() -> new NotFoundException("Not found shared post with id: " + basePostId));
        Post newPost = sharedPostMapper.convert(requestSharePostDto);
        newPost.setPostAuthor(user);
        postRepository.save(newPost);
        SharedPost sharedPost = SharedPost.builder()
                .sharedPostUser(user)
                .basePost(basePost)
                .newPost(newPost)
                .date(LocalDateTime.now())
                .build();
        sharedPostRepository.save(sharedPost);
    }

    @Override
    public void deleteSharedPostById(Long sharedPostId, Long userId) {
        if (sharedPostRepository.existsBySharedPostId(sharedPostId)) {
            User postSharedAuthor = sharedPostRepository.findById(sharedPostId).get().getSharedPostUser();
            if (!postSharedAuthor.getUserId().equals(userId)) {
                throw new ForbiddenException("Invalid shared post author id - shared post deleting access forbidden");
            }
            sharedPostRepository.deleteBySharedPostId(sharedPostId);
        } else {
            throw new NotFoundException("Not found shared post with id: " + sharedPostId);
        }

    }

    @Override
    public List<SharedPostDto> findAllSharedPosts() {
        List<SharedPost> sharedPosts = sharedPostRepository.findAll();
        return sharedPostDtoListMapper.convert(sharedPosts);
    }

}