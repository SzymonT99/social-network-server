package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.CommentRepository;
import com.server.springboot.domain.repository.PostRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class PostCommentServiceImpl implements PostCommentService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final JwtUtils jwtUtils;
    private final Converter<Comment, RequestCommentDto> commentMapper;

    @Autowired
    public PostCommentServiceImpl(UserRepository userRepository, PostRepository postRepository,
                                  CommentRepository commentRepository, JwtUtils jwtUtils,
                                  Converter<Comment, RequestCommentDto> commentMapper) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.jwtUtils = jwtUtils;
        this.commentMapper = commentMapper;
    }

    @Override
    public void addComment(Long postId, RequestCommentDto requestCommentDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User commentAuthor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found shared post with id: " + postId));
        Comment comment = commentMapper.convert(requestCommentDto);
        comment.setCommentAuthor(commentAuthor);
        comment.setCommentedPost(post);
        commentRepository.save(comment);
    }

    @Override
    public void editCommentById(Long commentId, RequestCommentDto requestCommentDto) {
        Long userId = jwtUtils.getLoggedUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found post comment with id: " + commentId));
        if (!comment.getCommentAuthor().getUserId().equals(userId)) {
            throw new ForbiddenException("Invalid comment author id - comment editing access forbidden");
        }
        comment.setEdited(true);
        comment.setEditedAt(LocalDateTime.now());
        comment.setText(requestCommentDto.getCommentText());
        commentRepository.save(comment);
    }

    @Override
    public void deleteCommentById(Long commentId) {
        Long userId = jwtUtils.getLoggedUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found post comment with id: " + commentId));
        if (!comment.getCommentAuthor().getUserId().equals(userId)) {
            throw new ForbiddenException("Invalid comment author id - comment deleting access forbidden");
        }
        commentRepository.deleteByCommentId(commentId);
    }

    @Override
    public void likeCommentById(Long commentId) {
        Long userId = jwtUtils.getLoggedUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found post comment with id: " + commentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Set<Comment> userLikedComment = user.getLikedComments();
        if (userLikedComment.contains(comment)) {
            throw new ConflictRequestException("The given comment has already been liked by user");
        }
        userLikedComment.add(comment);
        user.setLikedComments(userLikedComment);
        userRepository.save(user);
    }

    @Override
    public void dislikeCommentById(Long commentId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Comment deletedLikeFromComment = user.getLikedComments().stream()
                .filter(comment -> comment.getCommentId().equals(commentId))
                .findFirst().orElse(null);
        if (deletedLikeFromComment != null) {
            user.dislikeComment(deletedLikeFromComment);
            userRepository.save(user);
        } else {
            throw new BadRequestException("The user did not like the comment");
        }

    }
}
