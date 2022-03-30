package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.PostsPageDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.key.UserPostKey;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.GroupPermissionType;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.NotificationService;
import com.server.springboot.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final JwtUtils jwtUtils;
    private final FileService fileService;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final Converter<List<PostDto>, List<Post>> postDtoListMapper;
    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<Post, RequestSharePostDto> sharedPostMapper;
    private final Converter<List<SharedPostDto>, List<SharedPost>> sharedPostDtoListMapper;
    private final Converter<SharedPostDto, SharedPost> sharedPostDtoMapper;
    private final NotificationService notificationService;

    @Autowired
    public PostServiceImpl(Converter<Post, RequestPostDto> postMapper, UserRepository userRepository,
                           PostRepository postRepository, SharedPostRepository sharedPostRepository,
                           LikedPostRepository likedPostRepository, ImageRepository imageRepository,
                           JwtUtils jwtUtils, FileService fileService, GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository, Converter<List<PostDto>, List<Post>> postDtoListMapper,
                           Converter<PostDto, Post> postDtoMapper, Converter<Post, RequestSharePostDto> sharedPostMapper,
                           Converter<List<SharedPostDto>, List<SharedPost>> sharedPostDtoListMapper,
                           Converter<SharedPostDto, SharedPost> sharedPostDtoMapper,
                           NotificationService notificationService) {
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.sharedPostRepository = sharedPostRepository;
        this.likedPostRepository = likedPostRepository;
        this.imageRepository = imageRepository;
        this.jwtUtils = jwtUtils;
        this.fileService = fileService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.postDtoListMapper = postDtoListMapper;
        this.postDtoMapper = postDtoMapper;
        this.sharedPostMapper = sharedPostMapper;
        this.sharedPostDtoListMapper = sharedPostDtoListMapper;
        this.sharedPostDtoMapper = sharedPostDtoMapper;
        this.notificationService = notificationService;
    }

    @Override
    public PostsPageDto findAllPublicPosts(Integer page, Integer size) {
        Pageable paging = PageRequest.of(page, size);

        Page<Post> pagePosts;
        pagePosts = postRepository.findByIsDeletedAndIsPublicOrderByCreatedAtDesc(false, true, paging);
        List<Post> posts = pagePosts.getContent();

//        List<Post> postWithShares = sharedPostRepository.findAll().stream()
//                .map(SharedPost::getNewPost)
//                .collect(Collectors.toList());
//
//        posts.removeAll(postWithShares);    // ignorowanie postów które są udostępnieniem

        List<PostDto> postDtoList = postDtoListMapper.convert(posts);

        return PostsPageDto.builder()
                .posts(postDtoList)
                .currentPage(pagePosts.getNumber())
                .totalItems(pagePosts.getTotalElements())
                .totalPages(pagePosts.getTotalPages())
                .build();
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
    public PostDto addPost(RequestPostDto requestPostDto, List<MultipartFile> imageFiles, Long groupId) {
        Long userId = jwtUtils.getLoggedUserId();
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post addedPost = postMapper.convert(requestPostDto);
        addedPost.setPostAuthor(author);
        if (imageFiles != null) {
            Set<Image> postImages = fileService.storageImages(imageFiles, author);
            addedPost.setImages(postImages);
        }

        if (groupId != null) {
            Group group = groupRepository.findById(groupId).get();

            if (!groupMemberRepository.existsByMemberAndGroup(author, group)) {
                throw new ForbiddenException("User does not contain to group with id: " + groupId);
            }

            addedPost.setGroup(group);
            groupMemberRepository.setGroupMembersHasNewNotification(true, group);

            for (GroupMember groupMember : group.getGroupMembers()) {
                notificationService.sendNotificationToUser(author, groupMember.getMember().getUserId(), ActionType.ACTIVITY_BOARD);
            }

        }

        postRepository.save(addedPost);

        return postDtoMapper.convert(addedPost);
    }

    @Override
    public PostDto editPost(Long postId, RequestPostDto requestPostDto, List<MultipartFile> imageFiles) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));

        if (post.getGroup() != null) {
            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(post.getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, post.getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit the post");
            }
        } else {
            if (!post.getPostAuthor().getUserId().equals(userId)) {
                throw new ForbiddenException("Invalid post author id - post editing access forbidden");
            }
        }

        Set<Image> lastImages = new HashSet<>(post.getImages());
        if (lastImages.size() > 0) {
            post.removeImages();
            lastImages.forEach((lastImage -> {
                try {
                    fileService.deleteImage(lastImage.getImageId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        if (imageFiles != null) {
            Set<Image> updatedImages = fileService.storageImages(imageFiles, post.getPostAuthor());
            post.setImages(updatedImages);
        }

        post.setText(requestPostDto.getText());
        post.setPublic(Boolean.parseBoolean(requestPostDto.getIsPublic()));
        post.setEditedAt(LocalDateTime.now());
        post.setEdited(true);
        postRepository.save(post);
        imageRepository.deleteAll(lastImages);

        return postDtoMapper.convert(post);
    }

    @Override
    public void deletePostById(Long postId, boolean archive) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (post.getGroup() != null) {
            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(post.getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, post.getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit the post");
            }
        } else {
            if (!post.getPostAuthor().getUserId().equals(userId)) {
                throw new ForbiddenException("Invalid post author id - post editing access forbidden");
            }
        }

        if (archive) {
            post.setDeleted(true);
            postRepository.save(post);
        } else {
            Set<Image> lastImages = new HashSet<>(post.getImages());
            sharedPostRepository.deleteAll(post.getSharedBasePosts());
            postRepository.deleteByPostId(postId);
            imageRepository.deleteAll(lastImages);
            if (lastImages.size() > 0) {
                lastImages.forEach((lastImage -> {
                    try {
                        fileService.deleteImage(lastImage.getImageId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            }
        }
    }

    @Override
    public void likePost(Long postId) {
        Long userId = jwtUtils.getLoggedUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        User likedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        LikedPost newLikedPost = LikedPost.builder()
                .id(UserPostKey.builder().postId(postId).userId(userId).build())
                .post(post)
                .likedPostUser(likedUser)
                .date(LocalDateTime.now())
                .isPostAuthorNotified(false)
                .build();
        Set<LikedPost> likes = post.getLikedPosts();
        if (likedPostRepository.existsByPostAndLikedPostUser(post, likedUser)) {
            throw new ConflictRequestException("The user already liked this post");
        }
        likes.add(newLikedPost);
        post.setLikedPosts(likes);
        postRepository.save(post);

        notificationService.sendNotificationToUser(likedUser, post.getPostAuthor().getUserId(), ActionType.ACTIVITY_BOARD);
    }

    @Override
    public void deleteLikeFromPost(Long postId) {
        Long userId = jwtUtils.getLoggedUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        LikedPost likedPost = likedPostRepository.findByPostAndLikedPostUser(post, user)
                .orElseThrow(() -> new BadRequestException("The user does not like the post"));
        likedPostRepository.delete(likedPost);
    }

    @Override
    public SharedPostDto sharePost(Long basePostId, RequestSharePostDto requestSharePostDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
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
                .isPostAuthorNotified(false)
                .build();
        sharedPostRepository.save(sharedPost);

        notificationService.sendNotificationToUser(user, basePost.getPostAuthor().getUserId(), ActionType.ACTIVITY_BOARD);

        return sharedPostDtoMapper.convert(sharedPost);
    }

    @Override
    public void deleteSharedPostById(Long sharedPostId) {
        Long userId = jwtUtils.getLoggedUserId();
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

    @Override
    public void addPostToFavourite(Long postId) {
        Long userId = jwtUtils.getLoggedUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Set<Post> favouritePosts = user.getFavouritePosts();
        if (favouritePosts.contains(post)) {
            throw new ConflictRequestException("The given post has already been added to the user's favorites");
        }
        favouritePosts.add(post);
        user.setFavouritePosts(favouritePosts);
        userRepository.save(user);
    }

    @Override
    public void deletePostFromFavourite(Long postId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post deletedPost = user.getFavouritePosts().stream()
                .filter(post -> post.getPostId().equals(postId))
                .findFirst().orElse(null);
        if (deletedPost != null) {
            user.removePostFromFavourite(deletedPost);
            userRepository.save(user);
        } else {
            throw new BadRequestException("The post is not one of the user's favorite posts");
        }
    }

    @Override
    public List<PostDto> findAllFavouritePostsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<Post> favouritePosts = postRepository.findByFavourites(user);
        return postDtoListMapper.convert(favouritePosts);
    }

    @Override
    public void setPostCommentsAvailability(Long postId, boolean isBlocked) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (post.getGroup() != null) {
            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(post.getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, post.getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit the post");
            }
        } else {
            if (!post.getPostAuthor().getUserId().equals(userId)) {
                throw new ForbiddenException("Invalid post author id - post editing access forbidden");
            }
        }

        post.setCommentingBlocked(isBlocked);
        postRepository.save(post);
    }

    @Override
    public void setPostAccess(Long postId, boolean isPublic) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (post.getGroup() != null) {
            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(post.getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, post.getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit the post");
            }
        } else {
            if (!post.getPostAuthor().getUserId().equals(userId)) {
                throw new ForbiddenException("Invalid post author id - post editing access forbidden");
            }
        }

        post.setPublic(isPublic);
        postRepository.save(post);
    }
}
