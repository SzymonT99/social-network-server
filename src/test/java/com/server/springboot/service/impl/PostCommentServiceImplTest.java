package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.response.CommentDto;
import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.NotificationService;
import com.server.springboot.service.PostCommentService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostCommentServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Spy
    private CommentMapper commentMapper;
    @Spy
    private CommentDtoMapper commentDtoMapper;

    @InjectMocks
    private PostCommentServiceImpl postCommentService;

    private User user;
    private Post post;
    private Comment comment;


    @BeforeEach
    void setUp() {

        commentMapper = new CommentMapper();
        commentDtoMapper = new CommentDtoMapper();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        user = User.builder()
                .userId(1L)
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .likedComments(new HashSet<>())
                .verifiedAccount(true)
                .activityStatus(ActivityStatus.OFFLINE)
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

        comment = Comment.builder()
                .commentId(1L)
                .text("Komentarz")
                .commentAuthor(user)
                .commentedPost(post)
                .likes(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .isEdited(false)
                .build();
    }


    @Test
    public void shouldAddComment() {
        Long postId = 1L;
        RequestCommentDto requestCommentDto = RequestCommentDto.builder()
                .commentText("Komentarz")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        CommentDto createdComment = postCommentService.addComment(postId, requestCommentDto);

        assertNotNull(createdComment);
        assertEquals(requestCommentDto.getCommentText(), createdComment.getText());

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void shouldEditCommentById() {
        Long commentId = 1L;
        Set<Comment> postComments = new HashSet<>();
        postComments.add(comment);
        post.setComments(postComments);

        RequestCommentDto requestCommentDto = RequestCommentDto.builder()
                .commentText("Komentarz edytowany")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        comment.setEdited(true);
        comment.setEditedAt(LocalDateTime.now().plusHours(1L));

        postCommentService.editCommentById(commentId, requestCommentDto);

        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    public void shouldDeleteCommentById() {
        Long commentId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        postCommentService.deleteCommentById(commentId);

        verify(commentRepository, times(1)).deleteByCommentId(commentId);
    }

    @Test
    public void shouldThrowErrorWhenNotAuthorDeleteCommentById() {
        Long commentId = 1L;
        User commentAuthor = new User(user);
        commentAuthor.setUserId(2L);
        comment.setCommentAuthor(commentAuthor);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        assertThatExceptionOfType(ForbiddenException.class)
                .isThrownBy(() -> {
                    postCommentService.deleteCommentById(commentId);
                }).withMessage("Comment deleting access forbidden");

        verify(commentRepository, never()).deleteByCommentId(commentId);
    }

    @Test
    public void shouldLikeCommentById() {
        Long commentId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        postCommentService.likeCommentById(commentId);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldThrowErrorWhenCommentWasLikedByUserEarlier() {
        Long commentId = 1L;
        Set<Comment> likedComments = user.getLikedComments();
        likedComments.add(comment);
        user.setLikedComments(likedComments);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatExceptionOfType(ConflictRequestException.class)
                .isThrownBy(() -> {
                    postCommentService.likeCommentById(commentId);
                }).withMessage("The given comment has already been liked by user");

        verify(userRepository, never()).save(user);
    }

    @Test
    public void shouldDislikeCommentById() {
        Long commentId = 1L;
        Set<Comment> likedComments = user.getLikedComments();
        likedComments.add(comment);
        user.setLikedComments(likedComments);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() ->  postCommentService.dislikeCommentById(commentId));

        verify(userRepository, times(1)).save(user);
    }
}
