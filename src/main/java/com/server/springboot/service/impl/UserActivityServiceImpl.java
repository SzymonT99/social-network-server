package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActivityType;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserActivityServiceImpl implements UserActivityService {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PostRepository postRepository;
    private final LikedPostRepository likedPostRepository;
    private final CommentRepository commentRepository;
    private final SharedPostRepository sharedPostRepository;
    private final SharedEventRepository sharedEventRepository;
    private final EventMemberRepository eventMemberRepository;
    private final ImageRepository imageRepository;
    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<LikedPostActivityDto, LikedPost> likedPostActivityDtoMapper;
    private final Converter<CommentActivityDto, Comment> commentActivityDtoMapper;
    private final Converter<SharedPostDto, SharedPost> sharedPostDtoMapper;
    private final Converter<SharedEventDto, SharedEvent> sharedEventDtoMapper;
    private final Converter<EventReactionDto, EventMember> eventReactionDtoMapper;
    private final Converter<ChangeProfilePhotoDto, Image> changeProfilePhotoDtoMapper;

    @Autowired
    public UserActivityServiceImpl(JwtUtils jwtUtils, UserRepository userRepository, FriendRepository friendRepository,
                                   PostRepository postRepository, LikedPostRepository likedPostRepository,
                                   CommentRepository commentRepository, SharedPostRepository sharedPostRepository,
                                   SharedEventRepository sharedEventRepository, EventMemberRepository eventMemberRepository,
                                   ImageRepository imageRepository, Converter<PostDto, Post> postDtoMapper,
                                   Converter<LikedPostActivityDto, LikedPost> likedPostActivityDtoMapper,
                                   Converter<CommentActivityDto, Comment> commentActivityDtoMapper,
                                   Converter<SharedPostDto, SharedPost> sharedPostDtoMapper,
                                   Converter<SharedEventDto, SharedEvent> sharedEventDtoMapper,
                                   Converter<EventReactionDto, EventMember> eventReactionDtoMapper,
                                   Converter<ChangeProfilePhotoDto, Image> changeProfilePhotoDtoMapper) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.postRepository = postRepository;
        this.likedPostRepository = likedPostRepository;
        this.commentRepository = commentRepository;
        this.sharedPostRepository = sharedPostRepository;
        this.sharedEventRepository = sharedEventRepository;
        this.eventMemberRepository = eventMemberRepository;
        this.imageRepository = imageRepository;
        this.postDtoMapper = postDtoMapper;
        this.likedPostActivityDtoMapper = likedPostActivityDtoMapper;
        this.commentActivityDtoMapper = commentActivityDtoMapper;
        this.sharedPostDtoMapper = sharedPostDtoMapper;
        this.sharedEventDtoMapper = sharedEventDtoMapper;
        this.eventReactionDtoMapper = eventReactionDtoMapper;
        this.changeProfilePhotoDtoMapper = changeProfilePhotoDtoMapper;
    }

    @Override
    public List<BoardActivityItemDto> findUserActivityBoard() {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + loggedUserId));

        List<Friend> friends = friendRepository.findByUser(user);
        List<User> activeUsers = friends.stream().map(Friend::getUserFriend).collect(Collectors.toList());
        activeUsers.add(user);

        //TODO:: Posty z grup tematycznych

        List<Post> createdPosts = postRepository.findAllByPostAuthorInAndCreatedAtIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<LikedPost> likedPosts = likedPostRepository.findAllByLikedPostUserInAndDateIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<Comment> comments = commentRepository.findAllByCommentAuthorInAndCreatedAtIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<SharedPost> sharedPosts = sharedPostRepository.findAllBySharedPostUserInAndDateIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<SharedEvent> sharedEvents = sharedEventRepository.findAllBySharedEventUserInAndDateIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<EventMember> eventReaction = eventMemberRepository.findAllByEventMemberInAndAddedInIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<String> activeUsersProfilePhotosId = activeUsers.stream()
                .map(activeUser -> {
                    if (activeUser.getUserProfile().getProfilePhoto() != null) {
                        return activeUser.getUserProfile().getProfilePhoto().getImageId();
                    }
                    return null;
                })
                .collect(Collectors.toList());
        List<Image> profilePhotoChanges = imageRepository.findAllByImageIdInAndAddedInIsGreaterThan(
                activeUsersProfilePhotosId,
                LocalDateTime.now().minusWeeks(2));

        Map<LocalDateTime, Object> activityMap = new HashMap<LocalDateTime, Object>();

        createdPosts.forEach(post -> {
            activityMap.put(post.getCreatedAt(), postDtoMapper.convert(post));
        });

        likedPosts.forEach(likedPost -> {
            activityMap.put(likedPost.getDate(), likedPostActivityDtoMapper.convert(likedPost));
        });

        comments.forEach(comment -> {
            activityMap.put(comment.getCreatedAt(), commentActivityDtoMapper.convert(comment));
        });

        sharedPosts.forEach(sharedPost -> {
            activityMap.put(sharedPost.getDate(), sharedPostDtoMapper.convert(sharedPost));
        });

        sharedEvents.forEach(sharedEvent -> {
            activityMap.put(sharedEvent.getDate(), sharedEventDtoMapper.convert(sharedEvent));
        });

        eventReaction.forEach(eventMember -> {
            activityMap.put(eventMember.getAddedIn(), eventReactionDtoMapper.convert(eventMember));
        });

        profilePhotoChanges.forEach(image -> {
            activityMap.put(image.getAddedIn(), changeProfilePhotoDtoMapper.convert(image));
        });

        Map<LocalDateTime, Object> sortedActivityMapByDate = new TreeMap<>(activityMap).descendingMap();

        List<BoardActivityItemDto> boardActivity = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        sortedActivityMapByDate.forEach((k, v) -> {
            ActivityType activityType;
            UserDto activeUser;
            switch (v.getClass().getSimpleName()) {
                case "PostDto":
                    activityType = ActivityType.CREATE_POST;
                    activeUser = ((PostDto) v).getPostAuthor();
                    break;
                case "LikedPostActivityDto":
                    activityType = ActivityType.LIKE_POST;
                    activeUser = ((LikedPostActivityDto) v).getLikedUser();
                    break;
                case "CommentActivityDto":
                    activityType = ActivityType.COMMENT_POST;
                    activeUser = ((CommentActivityDto) v).getCommentAuthor();
                    break;
                case "SharedPostDto":
                    activityType = ActivityType.SHARE_POST;
                    activeUser = ((SharedPostDto) v).getAuthorOfSharing();
                    break;
                case "SharedEventDto":
                    activityType = ActivityType.SHARE_EVENT;
                    activeUser = ((SharedEventDto) v).getAuthorOfSharing();
                    break;
                case "EventReactionDto":
                    activityType = ActivityType.RESPOND_TO_EVENT;
                    activeUser = ((EventReactionDto) v).getEventMember();
                    break;
                case "ChangeProfilePhotoDto":
                    activityType = ActivityType.CHANGE_PROFILE_PHOTO;
                    activeUser = ((ChangeProfilePhotoDto) v).getUser();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + v.getClass().getSimpleName());
            }

            BoardActivityItemDto boardActivityItemDto = BoardActivityItemDto.builder()
                    .activityDate(k.format(formatter))
                    .activityType(activityType)
                    .activityAuthor(activeUser)
                    .activity(v)
                    .build();

            boardActivity.add(boardActivityItemDto);
        });

        return boardActivity;
    }
}
