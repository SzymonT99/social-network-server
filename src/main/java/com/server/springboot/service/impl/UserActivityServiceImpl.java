package com.server.springboot.service.impl;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActivityType;
import com.server.springboot.domain.enumeration.NotificationType;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.EventService;
import com.server.springboot.service.FriendService;
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
    private final FriendService friendService;
    private final EventService eventService;
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
    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public UserActivityServiceImpl(JwtUtils jwtUtils, FriendService friendService, EventService eventService,
                                   UserRepository userRepository, FriendRepository friendRepository,
                                   PostRepository postRepository, LikedPostRepository likedPostRepository,
                                   CommentRepository commentRepository, SharedPostRepository sharedPostRepository,
                                   SharedEventRepository sharedEventRepository, EventMemberRepository eventMemberRepository,
                                   ImageRepository imageRepository, Converter<PostDto, Post> postDtoMapper,
                                   Converter<LikedPostActivityDto, LikedPost> likedPostActivityDtoMapper,
                                   Converter<CommentActivityDto, Comment> commentActivityDtoMapper,
                                   Converter<SharedPostDto, SharedPost> sharedPostDtoMapper,
                                   Converter<SharedEventDto, SharedEvent> sharedEventDtoMapper,
                                   Converter<EventReactionDto, EventMember> eventReactionDtoMapper,
                                   Converter<ChangeProfilePhotoDto, Image> changeProfilePhotoDtoMapper,
                                   Converter<UserDto, User> userDtoMapper) {
        this.jwtUtils = jwtUtils;
        this.friendService = friendService;
        this.eventService = eventService;
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
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<BoardActivityItemDto> findUserActivityBoard() {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + loggedUserId));

        List<Friend> friends = friendRepository.findByUserAndIsInvitationAccepted(user, true);
        List<User> activeUsers = friends.stream().map(Friend::getUserFriend).collect(Collectors.toList());
        activeUsers.add(user);

        //TODO:: Posty z grup tematycznych

        List<Post> createdPosts = postRepository.findAllByPostAuthorInAndCreatedAtIsGreaterThanAndIsDeleted(
                activeUsers,
                LocalDateTime.now().minusWeeks(2),
                false);

        List<LikedPost> likedPosts = likedPostRepository.findAllByLikedPostUserInAndDateIsGreaterThan(
                activeUsers.stream().filter(activeUser -> !activeUser.getUserId().equals(loggedUserId)).collect(Collectors.toList()),
                LocalDateTime.now().minusWeeks(2));

        List<Comment> comments = commentRepository.findAllByCommentAuthorInAndCreatedAtIsGreaterThan(
                activeUsers.stream().filter(activeUser -> !activeUser.getUserId().equals(loggedUserId)).collect(Collectors.toList()),
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
                    .activityDate(k.toString())
                    .activityType(activityType)
                    .activityAuthor(activeUser)
                    .activity(v)
                    .build();

            boardActivity.add(boardActivityItemDto);
        });

        return boardActivity;
    }

    @Override
    public List<NotificationDto> findUserNotifications() {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + loggedUserId));

        List<Post> userPosts = Lists.newArrayList(user.getPosts());

        List<FriendInvitationDto> friendInvitations = friendService.findAllUserInvitationsToFriends(loggedUserId);

        List<EventInvitationDto> eventInvitations = eventService.findAllUserEventInvitation();

        List<LikedPost> likedPosts = likedPostRepository.findAllByPostIn(userPosts);
        likedPostRepository.setPostAuthorNotificationDisplayed(true, userPosts);

        List<Comment> comments = commentRepository.findAllByCommentedPostIn(userPosts);
        commentRepository.setPostAuthorNotificationDisplayed(true, userPosts);

        List<SharedPost> sharedPosts = sharedPostRepository.findAllByBasePostIn(userPosts);
        sharedPostRepository.setPostAuthorNotificationDisplayed(true, userPosts);

        List<Friend> acceptedFriends = friendRepository.findByUserAndIsInvitationAccepted(user, true);
        friendRepository.setUserNotificationDisplayed(true, user);

        List<NotificationDto> notificationList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        friendInvitations.forEach(friendInvitationDto -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.INVITATION_TO_FRIENDS)
                    .notificationDate(friendInvitationDto.getInvitationDate())
                    .isNotificationDisplayed(friendInvitationDto.getInvitationDisplayed())
                    .activityInitiator(friendInvitationDto.getInvitingUser())
                    .details(null)
                    .build();
            notificationList.add(notificationDto);
        });

        eventInvitations.forEach(eventInvitationDto -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.INVITATION_TO_EVENT)
                    .notificationDate(eventInvitationDto.getInvitationDate())
                    .isNotificationDisplayed(eventInvitationDto.isInvitationDisplayed())
                    .activityInitiator(null)
                    .details(Collections.singletonMap("eventId", eventInvitationDto.getEventId()))
                    .build();
            notificationList.add(notificationDto);
        });

        likedPosts.forEach(likedPost -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.LIKE_USER_POST)
                    .notificationDate(likedPost.getDate().format(formatter))
                    .isNotificationDisplayed(likedPost.isPostAuthorNotified())
                    .activityInitiator(userDtoMapper.convert(likedPost.getLikedPostUser()))
                    .details(Collections.singletonMap("postId", likedPost.getPost().getPostId()))
                    .build();
            notificationList.add(notificationDto);
        });

        comments.forEach(comment -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.COMMENT_USER_POST)
                    .notificationDate(comment.getCreatedAt().format(formatter))
                    .isNotificationDisplayed(comment.isPostAuthorNotified())
                    .activityInitiator(userDtoMapper.convert(comment.getCommentAuthor()))
                    .details(Collections.singletonMap("postId", comment.getCommentedPost().getPostId()))
                    .build();
            notificationList.add(notificationDto);
        });

        sharedPosts.forEach(sharedPost -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.SHARE_USER_POST)
                    .notificationDate(sharedPost.getDate().format(formatter))
                    .isNotificationDisplayed(sharedPost.isPostAuthorNotified())
                    .activityInitiator(userDtoMapper.convert(sharedPost.getSharedPostUser()))
                    .details(Collections.singletonMap("sharedPostId", sharedPost.getSharedPostId()))
                    .build();
            notificationList.add(notificationDto);
        });

        acceptedFriends.forEach(acceptedFriend -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.ACCEPTANCE_INVITATION_TO_FRIENDS)
                    .notificationDate(acceptedFriend.getFriendFromDate().format(formatter))
                    .isNotificationDisplayed(acceptedFriend.isUserNotifiedAboutAccepting())
                    .activityInitiator(userDtoMapper.convert(acceptedFriend.getUserFriend()))
                    .details(null)
                    .build();
            notificationList.add(notificationDto);
        });

        notificationList.sort(Comparator.comparing(NotificationDto::getNotificationDate));
        Collections.reverse(notificationList);

        return notificationList;
    }
}
