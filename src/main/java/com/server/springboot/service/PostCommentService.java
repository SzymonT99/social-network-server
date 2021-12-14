package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestCommentDto;

public interface PostCommentService {

    void addComment(Long postId, RequestCommentDto requestCommentDto);

    void editCommentById(Long commentId, RequestCommentDto requestCommentDto);

    void deleteCommentById(Long commentId, Long authorId);

    void likeCommentById(Long commentId, Long userId);

    void dislikeCommentById(Long commentId, Long userId);
}
