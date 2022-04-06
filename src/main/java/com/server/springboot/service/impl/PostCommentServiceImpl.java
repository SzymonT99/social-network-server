package com.server.springboot.service.impl;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.request.RequestCommentDto;
import com.server.springboot.domain.dto.response.CommentDto;
import com.server.springboot.domain.entity.Comment;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.GroupPermissionType;
import com.server.springboot.domain.mapper.CommentDtoMapper;
import com.server.springboot.domain.mapper.CommentMapper;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.NotificationService;
import com.server.springboot.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PostCommentServiceImpl implements PostCommentService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final JwtUtils jwtUtils;
    private final CommentMapper commentMapper;
    private final CommentDtoMapper commentDtoMapper;
    private final NotificationService notificationService;
    private final RoleRepository roleRepository;

    @Autowired
    public PostCommentServiceImpl(UserRepository userRepository, PostRepository postRepository,
                                  CommentRepository commentRepository, GroupMemberRepository groupMemberRepository, JwtUtils jwtUtils,
                                  CommentMapper commentMapper, CommentDtoMapper commentDtoMapper, NotificationService notificationService,
                                  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.jwtUtils = jwtUtils;
        this.commentMapper = commentMapper;
        this.commentDtoMapper = commentDtoMapper;
        this.notificationService = notificationService;
        this.roleRepository = roleRepository;
    }

    @Override
    public CommentDto addComment(Long postId, RequestCommentDto requestCommentDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User commentAuthor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found shared post with id: " + postId));
        Comment comment = commentMapper.convert(requestCommentDto);
        comment.setCommentAuthor(commentAuthor);
        comment.setCommentedPost(post);
        commentRepository.save(comment);

        notificationService.sendNotificationToUser(commentAuthor, post.getPostAuthor().getUserId(), ActionType.ACTIVITY_BOARD);

        return commentDtoMapper.convert(comment);
    }

    @Override
    public void editCommentById(Long commentId, RequestCommentDto requestCommentDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found post comment with id: " + commentId));

        if (comment.getCommentedPost().getGroup() != null &&
                !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(comment.getCommentedPost().getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, comment.getCommentedPost().getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit the post comment");
            }
        } else {
            if (!comment.getCommentAuthor().getUserId().equals(userId)
                    && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
                throw new ForbiddenException("Comment editing access forbidden");
            }
        }

        comment.setEdited(true);
        comment.setEditedAt(LocalDateTime.now());
        comment.setText(requestCommentDto.getCommentText());
        commentRepository.save(comment);
    }

    @Override
    public void deleteCommentById(Long commentId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found post comment with id: " + commentId));

        if (comment.getCommentedPost().getGroup() != null
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(comment.getCommentedPost().getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, comment.getCommentedPost().getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to delete the post comment");
            }
        } else {
            if (!comment.getCommentAuthor().getUserId().equals(userId)
                    && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
                throw new ForbiddenException("Comment deleting access forbidden");
            }
        }

        List<User> likedPostUsers = Lists.newArrayList(comment.getLikes());
        likedPostUsers.forEach((user -> {
            user.dislikeComment(comment);
            userRepository.save(user);
        }));

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
