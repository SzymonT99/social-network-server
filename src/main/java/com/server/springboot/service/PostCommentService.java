package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.dto.response.CommentDto;

public interface PostCommentService {

    CommentDto addComment(Long postId, RequestCommentDto requestCommentDto);

    void editCommentById(Long commentId, RequestCommentDto requestCommentDto);

    void deleteCommentById(Long commentId);

    void likeCommentById(Long commentId);

    void dislikeCommentById(Long commentId);
}
