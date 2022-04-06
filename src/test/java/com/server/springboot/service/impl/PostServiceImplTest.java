package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.request.RequestSharePostDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.dto.response.PostsPageDto;
import com.server.springboot.domain.dto.response.SharedPostDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.entity.key.UserPostKey;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.NotificationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private SharedPostRepository sharedPostRepository;
    @Mock
    private LikedPostRepository likedPostRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private FileService fileService;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RoleRepository roleRepository;
    @Spy
    private PostMapper postMapper;
    @Spy
    private PostDtoListMapper postDtoListMapper;
    @Spy
    private PostDtoMapper postDtoMapper;
    @Spy
    private SharedPostMapper sharedPostMapper;
    @Spy
    private SharedPostDtoListMapper sharedPostDtoListMapper;
    @Spy
    private SharedPostDtoMapper sharedPostDtoMapper;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post;
    private User user;

    @BeforeEach
    void setUp() {

        postMapper = new PostMapper();
        postDtoListMapper = new PostDtoListMapper();
        postDtoMapper = new PostDtoMapper();
        sharedPostMapper = new SharedPostMapper();
        sharedPostDtoListMapper = new SharedPostDtoListMapper();
        sharedPostDtoMapper = new SharedPostDtoMapper();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        user = User.builder()
                .userId(1L)
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .verifiedAccount(false)
                .activityStatus(ActivityStatus.OFFLINE)
                .isBlocked(false)
                .isBanned(false)
                .userProfile(UserProfile.builder()
                        .firstName("Jan")
                        .lastName("Nowak")
                        .gender(Gender.MALE)
                        .dateOfBirth(LocalDate.parse("1989-01-05", formatter))
                        .age(LocalDate.now().getYear() - LocalDate.parse("1989-01-05", formatter).getYear())
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();

        post = Post.builder()
                .postId(1L)
                .postAuthor(user)
                .text("Tresc")
                .images(new HashSet<>())
                .isPublic(false)
                .isEdited(false)
                .isDeleted(false)
                .isCommentingBlocked(false)
                .createdAt(LocalDateTime.now())
                .sharedBasePosts(new HashSet<>())
                .likedPosts(new HashSet<>())
                .comments(new HashSet<>())
                .build();
    }

    @Test
    public void shouldFindAllPublicPosts() {
        int page = 1;
        int size = 2;

        List<Post> savedPosts = new ArrayList<>();
        Post post2 = Post.builder()
                .postId(2L)
                .postAuthor(user)
                .text("Tresc 2")
                .images(new HashSet<>())
                .isPublic(false)
                .isEdited(false)
                .isDeleted(false)
                .isCommentingBlocked(false)
                .createdAt(LocalDateTime.now())
                .sharedBasePosts(new HashSet<>())
                .likedPosts(new HashSet<>())
                .comments(new HashSet<>())
                .build();

        savedPosts.add(post);
        savedPosts.add(post2);

        Pageable paging = PageRequest.of(page, size);
        Page<Post> pagePosts = new PageImpl<Post>(savedPosts, paging, savedPosts.size());

        when(postRepository.findByIsDeletedAndIsPublicOrderByCreatedAtDesc(false, true, paging)).thenReturn(pagePosts);

        PostsPageDto postsPageDto = postService.findAllPublicPosts(page, size);

        assertNotNull(postsPageDto);
        assertEquals(postDtoListMapper.convert(savedPosts), postsPageDto.getPosts());
        assertEquals(2, postsPageDto.getPosts().size());
    }

    @Test
    public void shouldFindPostById() {
        Long postId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostDto resultPost = postService.findPostById(postId);

        assertNotNull(resultPost);
        assertEquals(postDtoMapper.convert(post), resultPost);
    }

    @Test
    public void shouldAddPost() {
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .text("Tresc")
                .isPublic("false")
                .isCommentingBlocked("false")
                .build();
        MockMultipartFile uploadedImage = new MockMultipartFile("image", new byte[1]);
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(uploadedImage);
        Long groupId = null;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileService.storageImages(imageFiles, user)).thenReturn(new HashSet<Image>() {{
            add(new Image());
        }});

        PostDto createdPost = postService.addPost(requestPostDto, imageFiles, groupId);

        assertNotNull(createdPost);
        assertFalse(createdPost.isPublic());
        assertEquals(1, createdPost.getImages().size());

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    public void shouldEditPost() {
        Long postId = 1L;
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .text("Tresc edytowana")
                .isPublic("false")
                .isCommentingBlocked("false")
                .build();
        MockMultipartFile uploadedImage = new MockMultipartFile("image", new byte[1]);
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(uploadedImage);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostDto editedPost = postService.editPost(postId, requestPostDto, imageFiles);

        assertNotNull(editedPost);
        assertEquals(requestPostDto.getText(), editedPost.getText());

        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void shouldThrowErrorWhenPostAuthorIsInCorrect() {
        Long postId = 1L;
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .text("Tresc edytowana")
                .isPublic("false")
                .isCommentingBlocked("false")
                .build();
        List<MultipartFile> imageFiles = new ArrayList<>();

        User postAuthor = new User(user);
        postAuthor.setUserId(2L);
        post.setPostAuthor(postAuthor);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        Exception exception = assertThrows(ForbiddenException.class, () ->
                postService.editPost(postId, requestPostDto, imageFiles)
        );

        String expectedMessage = "Invalid post author id - post editing access forbidden";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        verify(postRepository, never()).save(post);
    }

    @Test
    public void shouldThrowErrorWhenPostNotFound() {
        Long postId = 1L;
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .text("Tresc edytowana")
                .isPublic("false")
                .isCommentingBlocked("false")
                .build();
        List<MultipartFile> imageFiles = new ArrayList<>();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                postService.editPost(postId, requestPostDto, imageFiles)
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void shouldDeletePostById() {
        Long postId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).deleteByPostId(postId);

        postService.deletePostById(postId, false);

        verify(postRepository, times(1)).deleteByPostId(postId);
    }

    @Test
    public void shouldDeletePostByIdWithArchiving() {
        Long postId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePostById(postId, true);

        verify(postRepository, times(1)).save(any(Post.class));
        verify(postRepository, never()).deleteByPostId(postId);
    }

    @Test
    public void shouldLikePost() {
        Long postId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likedPostRepository.existsByPostAndLikedPostUser(post, user)).thenReturn(false);

        assertDoesNotThrow(() -> postService.likePost(postId));

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    public void shouldThrowErrorWhenPostWasLikedEarlier() {
        Long postId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        when(likedPostRepository.existsByPostAndLikedPostUser(post, user)).thenReturn(true);

        Exception exception = assertThrows(ConflictRequestException.class, () ->
                postService.likePost(postId)
        );

        String expectedMessage = "The user already liked this post";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void shouldDeleteLikeFromPost() {
        Long postId = 1L;

        LikedPost likedPost = LikedPost.builder()
                .id(UserPostKey.builder().userId(1L).postId(1L).build())
                .post(post)
                .likedPostUser(user)
                .date(LocalDateTime.now().minusDays(1L))
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        when(likedPostRepository.findByPostAndLikedPostUser(post, user)).thenReturn(Optional.of(likedPost));

        postService.deleteLikeFromPost(postId);

        verify(likedPostRepository, times(1)).delete(likedPost);
    }

    @Test
    public void shouldSharePost() {
        Long basePostId = 1L;
        RequestSharePostDto requestSharePostDto = RequestSharePostDto.builder()
                .text("Tresc udostepnienia")
                .isPublic(true)
                .isCommentingBlocked(false)
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(basePostId)).thenReturn(Optional.of(post));

        SharedPostDto createdSharedPost = postService.sharePost(basePostId, requestSharePostDto);

        assertNotNull(createdSharedPost);
        assertEquals(requestSharePostDto.getText(), createdSharedPost.getSharingText());
        verify(sharedPostRepository, times(1)).save(any(SharedPost.class));
    }

    @Test
    public void shouldDeleteSharedPostById() {
        Long sharedPostId = 1L;
        SharedPost sharedPost = SharedPost.builder()
                .sharedPostId(1L)
                .basePost(post)
                .newPost(new Post())
                .sharedPostUser(user)
                .date(LocalDateTime.now().minusDays(1L))
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(sharedPostRepository.existsBySharedPostId(sharedPostId)).thenReturn(true);
        when(sharedPostRepository.findById(sharedPostId)).thenReturn(Optional.of(sharedPost));

        postService.deleteSharedPostById(sharedPostId);

        verify(sharedPostRepository, times(1)).deleteBySharedPostId(sharedPostId);
    }

    @Test
    public void shouldAddPostToFavourite() {
        Long postId = 1L;
        user.setFavouritePosts(new HashSet<>());

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertDoesNotThrow(() -> postService.addPostToFavourite(postId));

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldThrowErrorWhenPostWasAddedToFavouriteEarlier() {
        Long postId = 1L;
        Set<Post> favouritePosts = new HashSet<>();
        favouritePosts.add(post);
        user.setFavouritePosts(favouritePosts);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Exception exception = assertThrows(ConflictRequestException.class, () ->
                postService.addPostToFavourite(postId)
        );

        String expectedMessage = "The given post has already been added to the user's favorites";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
        verify(userRepository, never()).save(user);
    }


    @Test
    public void shouldDeletePostFromFavourite() {
        Long postId = 1L;
        Set<Post> favouritePosts = new HashSet<>();
        favouritePosts.add(post);
        user.setFavouritePosts(favouritePosts);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> postService.deletePostFromFavourite(postId));

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldThrowErrorWhenDeletePostNotBelongingToFavourite() {
        Long postId = 1L;
        Set<Post> favouritePosts = new HashSet<>();
        user.setFavouritePosts(favouritePosts);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(BadRequestException.class, () ->
                postService.deletePostFromFavourite(postId)
        );

        String expectedMessage = "The post is not one of the user's favorite posts";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
        verify(userRepository, never()).save(user);
    }

    @Test
    public void shouldFindAllFavouritePostsByUserId() {
        Long userId = 1L;

        List<Post> savedFavouritePosts = new ArrayList<>();
        Post post2 = Post.builder()
                .postId(2L)
                .postAuthor(new User(user))
                .text("Tresc 2")
                .images(new HashSet<>())
                .isPublic(false)
                .isEdited(false)
                .isDeleted(false)
                .isCommentingBlocked(false)
                .createdAt(LocalDateTime.now())
                .sharedBasePosts(new HashSet<>())
                .likedPosts(new HashSet<>())
                .comments(new HashSet<>())
                .build();

        savedFavouritePosts.add(post);
        savedFavouritePosts.add(post2);
        user.setFavouritePosts(new HashSet<>(savedFavouritePosts));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findByFavourites(user)).thenReturn(savedFavouritePosts);

        List<PostDto> resultFavouritePosts = postService.findAllFavouritePostsByUserId(userId);

        assertEquals(postDtoListMapper.convert(savedFavouritePosts), resultFavouritePosts);
        assertEquals(2, resultFavouritePosts.size());
    }

    @Test
    public void shouldSetPostCommentsAvailability() {
        Long postId = 1L;
        boolean isBlocked = true;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertDoesNotThrow(() ->  postService.setPostCommentsAvailability(postId, isBlocked));

        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void shouldSetPostAccess() {
        Long postId = 1L;
        boolean isPublic = true;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertDoesNotThrow(() ->  postService.setPostAccess(postId, isPublic));

        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void shouldThrowErrorWhenUnauthorizedUserWantToSetPostAccess() {
        Long postId = 1L;
        boolean isPublic = false;
        User postAuthor = new User(user);
        postAuthor.setUserId(2L);
        post.setPostAuthor(postAuthor);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        Exception exception = assertThrows(ForbiddenException.class, () ->
                postService.setPostAccess(postId, isPublic)
        );

        String expectedMessage = "Invalid post author id - post editing access forbidden";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        verify(postRepository, never()).save(post);
    }
}

