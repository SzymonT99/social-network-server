package com.server.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.mapper.UserDtoMapper;
import com.server.springboot.domain.repository.LikedPostRepository;
import com.server.springboot.domain.repository.PostRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.service.impl.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostApiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDtoMapper userDtoMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikedPostRepository likedPostRepository;

    @Test
    @Transactional
    public void shouldCreatePost() throws Exception {
        User user = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        RequestPostDto requestPostDto = RequestPostDto.builder()
                .text("Tresc")
                .isPublic("false")
                .isCommentingBlocked("false")
                .build();

        MockMultipartFile file = new MockMultipartFile("image", new byte[1]);
        MockMultipartFile postJson = new MockMultipartFile("post", null,
                "application/json", objectMapper.writeValueAsString(requestPostDto).getBytes());

        mockMvc.perform(multipart("/api/posts")
                .file(file)
                .file(postJson)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value(requestPostDto.getText()))
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.isPublic").value(Boolean.valueOf(requestPostDto.getIsPublic())))
                .andExpect(jsonPath("$.isCommentingBlocked").value(requestPostDto.getIsCommentingBlocked()))
                .andExpect(jsonPath("$.postAuthor").value(userDtoMapper.convert(user)))
                .andDo(print());
    }

    @Test
    public void shouldGetPublicPosts() throws Exception {
        int page = 0;
        int size = 6;
        Pageable paging = PageRequest.of(page, size);
        Page<Post> pagePosts = postRepository.findByIsDeletedAndIsPublicOrderByCreatedAtDesc(false, true, paging);
        List<Post> posts = pagePosts.getContent();

        mockMvc.perform(get("/api/posts")
                .with(anonymous())
                .param("page", "0")
                .param("size", "6"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.posts.size()").value(posts.size()))
                .andDo(print());
    }

    @Test
    public void shouldLikePostById() throws Exception {
        User user = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        Long postId = 1L;
        Post post = postRepository.findById(postId).get();

        mockMvc.perform(post("/api/posts/{postId}/like", postId)
                .with(user(userDetails)))
                .andExpect(status().isCreated())
                .andDo(print());

        assertTrue(likedPostRepository.existsByPostAndLikedPostUser(post, user));
    }

    @Test
    public void shouldAddCommentOnPost() throws Exception {
        User user = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        Long postId = 1L;

        RequestCommentDto requestCommentDto = RequestCommentDto.builder()
                .commentText("Komentarz")
                .build();

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                .with(user(userDetails))
                .content(objectMapper.writeValueAsString(requestCommentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value(requestCommentDto.getCommentText()))
                .andExpect(jsonPath("$.commentAuthor").value(userDtoMapper.convert(user)))
                .andDo(print());
    }
}
