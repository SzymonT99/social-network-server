package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.repository.CommentRepository;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.service.PostCommentService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class PostCommentServiceImplTest {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("Add comment on post with id: 131")
    @Order(value = 1)
    void addComment() {

        RequestCommentDto requestCommentDto = RequestCommentDto.builder()
                .commentText("Test")
                .build();

        postCommentService.addComment(131L, requestCommentDto);

        assertTrue(commentRepository.findById(165L).get().getCommentedPost().getPostId() == 131L
                && commentRepository.findById(165L).get().getText().equals("Test"));
    }

    @Test
    @DisplayName("Delete comment with id: 165")
    @Order(value = 2)
    void deleteCommentById() {
        postCommentService.deleteCommentById(165L);

        assertThatThrownBy(() -> commentRepository.findById(165L))
                .isInstanceOf(BadRequestException.class);
    }

}
