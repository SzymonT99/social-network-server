package com.server.springboot.service.impl;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.ActivityType;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.enumeration.NotificationType;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.EventService;
import com.server.springboot.service.GroupService;
import com.server.springboot.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserActivityServiceImpl implements UserActivityService {

    private final JwtUtils jwtUtils;
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
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;
    private final Converter<PostDto, Post> postDtoMapper;
    private final Converter<GroupPostDto, Post> groupPostDtoMapper;
    private final Converter<LikedPostActivityDto, LikedPost> likedPostActivityDtoMapper;
    private final Converter<LikedSharedPostActivityDto, LikedPost> likedSharedPostActivityDtoMapper;
    private final Converter<CommentActivityDto, Comment> commentActivityDtoMapper;
    private final Converter<SharedPostCommentActivityDto, Comment> sharedPostCommentActivityDtoMapper;
    private final Converter<SharedPostDto, SharedPost> sharedPostDtoMapper;
    private final Converter<SharedEventDto, SharedEvent> sharedEventDtoMapper;
    private final Converter<EventReactionDto, EventMember> eventReactionDtoMapper;
    private final Converter<GroupJoiningDto, GroupMember> groupJoiningDtoMapper;
    private final Converter<ChangeProfilePhotoDto, Image> changeProfilePhotoDtoMapper;
    private final Converter<GroupDto, Group> groupDtoMapper;
    private final Converter<UserDto, User> userDtoMapper;
    private final GroupRepository groupRepository;

    @Autowired
    public UserActivityServiceImpl(JwtUtils jwtUtils, EventService eventService,
                                   UserRepository userRepository, FriendRepository friendRepository,
                                   PostRepository postRepository, LikedPostRepository likedPostRepository,
                                   CommentRepository commentRepository, SharedPostRepository sharedPostRepository,
                                   SharedEventRepository sharedEventRepository, EventMemberRepository eventMemberRepository,
                                   ImageRepository imageRepository,
                                   GroupService groupService, GroupMemberRepository groupMemberRepository,
                                   Converter<PostDto, Post> postDtoMapper,
                                   Converter<GroupPostDto, Post> groupPostDtoMapper,
                                   Converter<LikedPostActivityDto, LikedPost> likedPostActivityDtoMapper,
                                   Converter<LikedSharedPostActivityDto, LikedPost> likedSharedPostActivityDtoMapper,
                                   Converter<CommentActivityDto, Comment> commentActivityDtoMapper,
                                   Converter<SharedPostCommentActivityDto, Comment> sharedPostCommentActivityDtoMapper,
                                   Converter<SharedPostDto, SharedPost> sharedPostDtoMapper,
                                   Converter<SharedEventDto, SharedEvent> sharedEventDtoMapper,
                                   Converter<EventReactionDto, EventMember> eventReactionDtoMapper,
                                   Converter<GroupJoiningDto, GroupMember> groupJoiningDtoMapper,
                                   Converter<ChangeProfilePhotoDto, Image> changeProfilePhotoDtoMapper,
                                   Converter<GroupDto, Group> groupDtoMapper, Converter<UserDto, User> userDtoMapper, GroupRepository groupRepository) {
        this.jwtUtils = jwtUtils;
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
        this.likedSharedPostActivityDtoMapper = likedSharedPostActivityDtoMapper;
        this.groupService = groupService;
        this.groupMemberRepository = groupMemberRepository;
        this.postDtoMapper = postDtoMapper;
        this.groupPostDtoMapper = groupPostDtoMapper;
        this.likedPostActivityDtoMapper = likedPostActivityDtoMapper;
        this.commentActivityDtoMapper = commentActivityDtoMapper;
        this.sharedPostCommentActivityDtoMapper = sharedPostCommentActivityDtoMapper;
        this.sharedPostDtoMapper = sharedPostDtoMapper;
        this.sharedEventDtoMapper = sharedEventDtoMapper;
        this.eventReactionDtoMapper = eventReactionDtoMapper;
        this.groupJoiningDtoMapper = groupJoiningDtoMapper;
        this.changeProfilePhotoDtoMapper = changeProfilePhotoDtoMapper;
        this.groupDtoMapper = groupDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.groupRepository = groupRepository;
    }

    @Override
    public List<BoardActivityItemDto> findUserActivityBoard() {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + loggedUserId));

        List<Friend> friends = friendRepository.findByUserAndIsInvitationAccepted(user, true);
        List<User> activeUsers = friends.stream().map(Friend::getUserFriend).collect(Collectors.toList());
        activeUsers.add(user);

        List<Group> userGroups = groupMemberRepository.findByMemberAndGroupMemberStatus(user, GroupMemberStatus.JOINED).stream()
                .map(GroupMember::getGroup)
                .collect(Collectors.toList());

        List<Post> createdPosts = postRepository.findAllByPostAuthorInAndCreatedAtIsGreaterThanAndIsDeleted(
                activeUsers,
                LocalDateTime.now().minusWeeks(2),
                false).stream()
                .filter(post -> post.getGroup() == null && post.getChangedProfileImage() == null && post.getSharedNewPost() == null)
                .collect(Collectors.toList());

        List<Post> groupPosts = postRepository.findAllByGroupInAndCreatedAtIsGreaterThanAndIsDeleted(
                userGroups,
                LocalDateTime.now().minusWeeks(2),
                false);

        List<LikedPost> likedPosts = likedPostRepository.findAllByLikedPostUserInAndDateIsGreaterThan(
                activeUsers.stream().filter(activeUser -> !activeUser.getUserId().equals(loggedUserId)).collect(Collectors.toList()),
                LocalDateTime.now().minusWeeks(2)).stream()
                .filter(likedPost -> likedPost.getPost().getSharedNewPost() == null
                        && !likedPost.getPost().isDeleted() && likedPost.getPost().getChangedProfileImage() == null)
                .collect(Collectors.toList());

        List<LikedPost> likedSharedPosts = likedPostRepository.findAllByLikedPostUserInAndDateIsGreaterThan(
                activeUsers.stream().filter(activeUser -> !activeUser.getUserId().equals(loggedUserId)).collect(Collectors.toList()),
                LocalDateTime.now().minusWeeks(2)).stream()
                .filter(likedPost -> likedPost.getPost().getSharedNewPost() != null && !likedPost.getPost().isDeleted())
                .collect(Collectors.toList());

        List<Comment> comments = commentRepository.findAllByCommentAuthorInAndCreatedAtIsGreaterThan(
                activeUsers.stream().filter(activeUser -> !activeUser.getUserId().equals(loggedUserId)).collect(Collectors.toList()),
                LocalDateTime.now().minusWeeks(2)).stream()
                .filter(comment -> comment.getCommentedPost().getSharedNewPost() == null
                        && !comment.getCommentedPost().isDeleted() && comment.getCommentedPost().getChangedProfileImage() == null)
                .collect(Collectors.toList());

        List<Comment> commentedSharedPosts = commentRepository.findAllByCommentAuthorInAndCreatedAtIsGreaterThan(
                activeUsers.stream().filter(activeUser -> !activeUser.getUserId().equals(loggedUserId)).collect(Collectors.toList()),
                LocalDateTime.now().minusWeeks(2)).stream()
                .filter(comment -> comment.getCommentedPost().getSharedNewPost() != null && !comment.getCommentedPost().isDeleted())
                .collect(Collectors.toList());

        List<SharedPost> sharedPosts = sharedPostRepository.findAllBySharedPostUserInAndDateIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<SharedEvent> sharedEvents = sharedEventRepository.findAllBySharedEventUserInAndDateIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<EventMember> eventReaction = eventMemberRepository.findAllByEventMemberInAndAddedInIsGreaterThan(
                activeUsers,
                LocalDateTime.now().minusWeeks(2));

        List<GroupMember> groupJoining = groupMemberRepository.findAllByMemberInAndGroupMemberStatusAndAddedInIsGreaterThan(
                activeUsers,
                GroupMemberStatus.JOINED,
                LocalDateTime.now().minusWeeks(2)).stream()
                .filter(groupMember -> !groupMember.getGroup().isDeleted()).collect(Collectors.toList());

        List<String> activeUsersProfilePhotosId = activeUsers.stream()
                .map(activeUser -> {
                    if (activeUser.getUserProfile().getProfilePhoto() != null) {
                        return activeUser.getUserProfile().getProfilePhoto().getImageId();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        activeUsersProfilePhotosId.removeIf(Objects::isNull);

        List<Image> profilePhotoChanges = imageRepository.findAllByImageIdInAndAddedInIsGreaterThan(
                activeUsersProfilePhotosId,
                LocalDateTime.now().minusWeeks(2));

        Map<LocalDateTime, Object> activityMap = new HashMap<LocalDateTime, Object>();

        createdPosts.forEach(post -> {
            activityMap.put(post.getCreatedAt(), postDtoMapper.convert(post));
        });

        groupPosts.forEach(groupPost -> {
            activityMap.put(groupPost.getCreatedAt(), groupPostDtoMapper.convert(groupPost));
        });

        likedPosts.forEach(likedPost -> {
            activityMap.put(likedPost.getDate(), likedPostActivityDtoMapper.convert(likedPost));
        });

        likedSharedPosts.forEach(likedPost -> {
            activityMap.put(likedPost.getDate(), likedSharedPostActivityDtoMapper.convert(likedPost));
        });

        comments.forEach(comment -> {
            activityMap.put(comment.getCreatedAt(), commentActivityDtoMapper.convert(comment));
        });

        commentedSharedPosts.forEach(comment -> {
            activityMap.put(comment.getCreatedAt(), sharedPostCommentActivityDtoMapper.convert(comment));
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

        groupJoining.forEach(groupMember -> {
            activityMap.put(groupMember.getAddedIn(), groupJoiningDtoMapper.convert(groupMember));
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
                case "GroupPostDto":
                    activityType = ActivityType.CREATE_GROUP_POST;
                    activeUser = ((GroupPostDto) v).getPostAuthor();
                    break;
                case "LikedPostActivityDto":
                    activityType = ActivityType.LIKE_POST;
                    activeUser = ((LikedPostActivityDto) v).getLikedUser();
                    break;
                case "LikedSharedPostActivityDto":
                    activityType = ActivityType.LIKE_SHARED_POST;
                    activeUser = ((LikedSharedPostActivityDto) v).getLikedUser();
                    break;
                case "CommentActivityDto":
                    activityType = ActivityType.COMMENT_POST;
                    activeUser = ((CommentActivityDto) v).getCommentAuthor();
                    break;
                case "SharedPostCommentActivityDto":
                    activityType = ActivityType.COMMENT_SHARED_POST;
                    activeUser = ((SharedPostCommentActivityDto) v).getCommentAuthor();
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
                case "GroupJoiningDto":
                    activityType = ActivityType.JOIN_TO_GROUP;
                    activeUser = ((GroupJoiningDto) v).getUserMember();
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
    public List<NotificationDto> findUserNotifications(boolean isDisplayed) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + loggedUserId));

        List<Post> userPosts = Lists.newArrayList(user.getPosts());

        List<Group> userGroups = groupMemberRepository.findByMemberAndGroupMemberStatus(user, GroupMemberStatus.JOINED).stream()
                .map(GroupMember::getGroup)
                .collect(Collectors.toList());

        List<GroupMember> userGroupMemberList = groupMemberRepository.findByMemberAndGroupMemberStatus(user, GroupMemberStatus.JOINED);

        if (isDisplayed) {
            likedPostRepository.setPostAuthorNotificationDisplayed(true, userPosts);
            commentRepository.setPostAuthorNotificationDisplayed(true, userPosts);
            sharedPostRepository.setPostAuthorNotificationDisplayed(true, userPosts);
            friendRepository.setUserNotificationDisplayed(true, user);
            groupMemberRepository.setGroupMemberNotification(false, userGroups);
        }

        List<EventInvitationDto> eventInvitations = eventService.findAllUserEventInvitation(isDisplayed);

        List<LikedPost> likedPosts = likedPostRepository.findAllByPostIn(userPosts);

        List<Comment> comments = commentRepository.findAllByCommentedPostIn(userPosts);

        List<SharedPost> sharedPosts = sharedPostRepository.findAllByBasePostIn(userPosts);

        List<Friend> acceptedFriends = friendRepository.findByUserFriendAndIsInvitationAccepted(user, true);

        List<Post> groupPosts = postRepository.findAllByGroupIn(userGroups);

        List<GroupInvitationDto> groupInvitations = groupService.findAllUserGroupInvitations(isDisplayed);

        List<GroupMember> addedGroupMember = userGroupMemberList.stream().
                filter(groupMember -> groupMember.getInvitationDate() == null)
                .collect(Collectors.toList());

        List<NotificationDto> notificationList = new ArrayList<>();

        eventInvitations.forEach(eventInvitationDto -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.INVITATION_TO_EVENT)
                    .notificationDate(eventInvitationDto.getInvitationDate())
                    .isNotificationDisplayed(eventInvitationDto.isInvitationDisplayed())
                    .activityInitiator(eventInvitationDto.getEventAuthor())
                    .details(Collections.singletonMap("eventId", eventInvitationDto.getEventId()))
                    .build();
            notificationList.add(notificationDto);
        });

        likedPosts.forEach(likedPost -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.LIKE_USER_POST)
                    .notificationDate(likedPost.getDate().toString())
                    .isNotificationDisplayed(likedPost.isPostAuthorNotified())
                    .activityInitiator(userDtoMapper.convert(likedPost.getLikedPostUser()))
                    .details(Collections.singletonMap("postId", likedPost.getPost().getPostId()))
                    .build();
            notificationList.add(notificationDto);
        });

        comments.forEach(comment -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.COMMENT_USER_POST)
                    .notificationDate(comment.getCreatedAt().toString())
                    .isNotificationDisplayed(comment.isPostAuthorNotified())
                    .activityInitiator(userDtoMapper.convert(comment.getCommentAuthor()))
                    .details(Collections.singletonMap("postId", comment.getCommentedPost().getPostId()))
                    .build();
            notificationList.add(notificationDto);
        });

        sharedPosts.forEach(sharedPost -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.SHARE_USER_POST)
                    .notificationDate(sharedPost.getDate().toString())
                    .isNotificationDisplayed(sharedPost.isPostAuthorNotified())
                    .activityInitiator(userDtoMapper.convert(sharedPost.getSharedPostUser()))
                    .details(Collections.singletonMap("postId", sharedPost.getBasePost().getPostId()))
                    .build();
            notificationList.add(notificationDto);
        });

        acceptedFriends.forEach(acceptedFriend -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.ACCEPTANCE_INVITATION_TO_FRIENDS)
                    .notificationDate(acceptedFriend.getFriendFromDate().toString())
                    .isNotificationDisplayed(acceptedFriend.isUserNotifiedAboutAccepting())
                    .activityInitiator(userDtoMapper.convert(acceptedFriend.getUser()))
                    .details(Collections.singletonMap("userFriendId", acceptedFriend.getUser().getUserId()))
                    .build();
            notificationList.add(notificationDto);
        });

        groupPosts.forEach(groupPost -> {

            GroupMember currentMember = groupMemberRepository.findByGroupAndMember(groupPost.getGroup(), user)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            loggedUserId, groupPost.getGroup().getGroupId())));

            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.POST_IN_GROUP)
                    .notificationDate(groupPost.getCreatedAt().toString())
                    .isNotificationDisplayed(!currentMember.isHasNotification())
                    .activityInitiator(userDtoMapper.convert(groupPost.getPostAuthor()))
                    .details(Collections.singletonMap("groupId", groupPost.getGroup().getGroupId()))
                    .build();
            notificationList.add(notificationDto);
        });

        groupInvitations.forEach(groupInvitation -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.INVITATION_TO_GROUP)
                    .notificationDate(groupInvitation.getInvitationDate())
                    .isNotificationDisplayed(groupInvitation.isInvitationDisplayed())
                    .activityInitiator(groupInvitation.getGroupCreator())
                    .details(groupDtoMapper.convert(groupRepository.findById(groupInvitation.getGroupId()).get()))
                    .build();
            notificationList.add(notificationDto);
        });

        addedGroupMember.forEach(groupMember -> {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationType(NotificationType.ADDED_TO_GROUP)
                    .notificationDate(groupMember.getAddedIn().toString())
                    .isNotificationDisplayed(!groupMember.isHasNotification())
                    .activityInitiator(userDtoMapper.convert(groupMember.getGroup().getGroupCreator()))
                    .details(groupDtoMapper.convert(groupMember.getGroup()))
                    .build();
            notificationList.add(notificationDto);
        });

        notificationList.sort(Comparator.comparing(NotificationDto::getNotificationDate).reversed());

        return notificationList
                .stream()
                .filter(n -> !n.getActivityInitiator().getUserId().equals(loggedUserId) &&
                        LocalDateTime.parse(n.getNotificationDate()).isAfter(LocalDateTime.now().minusWeeks(2L)))
                .collect(Collectors.toList());
    }
}
