package com.server.springboot.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.springboot.domain.dto.request.CreateUserDto;
import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.PostRepository;
import com.server.springboot.domain.repository.ReportRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.PostService;
import org.junit.jupiter.api.*;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class PostServiceImplTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private Converter<UserDto, User> userDtoMapper;

    @Test
    @DisplayName("Add post")
    @Order(value = 1)
    void addPost() {

        Long userId = jwtUtils.getLoggedUserId();

        RequestPostDto requestPostDto = RequestPostDto.builder()
                .text("Test")
                .isPublic("true")
                .isCommentingBlocked("false")
                .build();

        postService.addPost(requestPostDto, null);

        assertTrue(new ReflectionEquals(PostDto
                .builder()
                .postId(131L)
                .text("Test")
                .images(null)
                .editedAt(null)
                .postAuthor(userDtoMapper.convert(userRepository.findById(userId).get()))
                .isPublic(true)
                .isCommentingBlocked(false)
                .isEdited(false)
                .likes(null)
                .comments(null)
                .sharing(null)
                .build()
        ).matches(postService.findPostById(131L)));
    }

    @Test
    @DisplayName("Find post by id: 131")
    @Order(value = 2)
    void findPostById() {

        Long userId = jwtUtils.getLoggedUserId();

        assertTrue(new ReflectionEquals(PostDto
                .builder()
                .postId(131L)
                .text("Test")
                .images(null)
                .editedAt(null)
                .postAuthor(userDtoMapper.convert(userRepository.findById(userId).get()))
                .isPublic(true)
                .isCommentingBlocked(false)
                .isEdited(false)
                .likes(null)
                .comments(null)
                .sharing(null)
                .build()
        ).matches(postService.findPostById(131L)));
    }

    @Test
    @DisplayName("Delete post by id: 131")
    @Order(value = 3)
    void deleteUserPostById() {
        postService.deleteUserPostById(131L, false);

        assertThatThrownBy(() -> postService.findPostById(131L))
                .isInstanceOf(NotFoundException.class);
    }

}

