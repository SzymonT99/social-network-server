package com.server.springboot.service.impl;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.enumeration.GroupPermissionType;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupRuleRepository groupRuleRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final Converter<Group, RequestGroupDto> groupMapper;
    private final FileService fileService;
    private final JwtUtils jwtUtils;
    private final ImageRepository imageRepository;
    private final InterestRepository interestRepository;
    private final GroupThreadRepository groupThreadRepository;
    private final ThreadAnswerRepository threadAnswerRepository;
    private final ThreadAnswerReviewRepository threadAnswerReviewRepository;
    private final Converter<List<GroupDto>, List<Group>> groupDtoListMapper;
    private final Converter<GroupDetailsDto, Group> groupDetailsDtoMapper;
    private final Converter<GroupRule, RequestGroupRuleDto> groupRuleMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;
    private final Converter<List<GroupInvitationDto>, List<GroupMember>> groupInvitationDtoListMapper;
    private final Converter<List<PostDto>, List<Post>> postDtoListMapper;
    private final Converter<GroupThread, RequestThreadDto> groupThreadMapper;
    private final Converter<List<UserDto>, List<User>> userDtoListMapper;
    private final Converter<List<GroupMemberDto>, List<GroupMember>> groupMemberDtoListMapper;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, GroupRuleRepository groupRuleRepository,
                            GroupMemberRepository groupMemberRepository, UserRepository userRepository,
                            Converter<Group, RequestGroupDto> groupMapper, FileService fileService, JwtUtils jwtUtils,
                            ImageRepository imageRepository, InterestRepository interestRepository,
                            GroupThreadRepository groupThreadRepository,
                            ThreadAnswerRepository threadAnswerRepository,
                            ThreadAnswerReviewRepository threadAnswerReviewRepository,
                            Converter<List<GroupDto>, List<Group>> groupDtoListMapper,
                            Converter<GroupDetailsDto, Group> groupDetailsDtoMapper,
                            Converter<GroupRule, RequestGroupRuleDto> groupRuleMapper,
                            Converter<List<InterestDto>, List<Interest>> interestDtoListMapper,
                            Converter<List<GroupInvitationDto>, List<GroupMember>> groupInvitationDtoListMapper,
                            Converter<List<PostDto>, List<Post>> postDtoListMapper,
                            Converter<GroupThread, RequestThreadDto> groupThreadMapper, Converter<List<UserDto>, List<User>> userDtoListMapper, Converter<List<GroupMemberDto>, List<GroupMember>> groupMemberDtoListMapper) {
        this.groupRepository = groupRepository;
        this.groupRuleRepository = groupRuleRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupMapper = groupMapper;
        this.fileService = fileService;
        this.jwtUtils = jwtUtils;
        this.imageRepository = imageRepository;
        this.interestRepository = interestRepository;
        this.groupThreadRepository = groupThreadRepository;
        this.threadAnswerRepository = threadAnswerRepository;
        this.threadAnswerReviewRepository = threadAnswerReviewRepository;
        this.groupDtoListMapper = groupDtoListMapper;
        this.groupDetailsDtoMapper = groupDetailsDtoMapper;
        this.groupRuleMapper = groupRuleMapper;
        this.interestDtoListMapper = interestDtoListMapper;
        this.groupInvitationDtoListMapper = groupInvitationDtoListMapper;
        this.postDtoListMapper = postDtoListMapper;
        this.groupThreadMapper = groupThreadMapper;
        this.userDtoListMapper = userDtoListMapper;
        this.groupMemberDtoListMapper = groupMemberDtoListMapper;
    }

    @Override
    public void addGroup(RequestGroupDto requestGroupDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedUserId();
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + (userId)));

        Group createdGroup = groupMapper.convert(requestGroupDto);
        createdGroup.setGroupCreator(creator);

        if (imageFile != null) {
            Image image = fileService.storageOneImage(imageFile, creator, false);
            createdGroup.setImage(image);
        }

        GroupMember groupMember = GroupMember.builder()
                .groupPermissionType(GroupPermissionType.ADMINISTRATOR)
                .groupMemberStatus(GroupMemberStatus.JOINED)
                .addedIn(LocalDateTime.now())
                .member(creator)
                .group(createdGroup)
                .build();

        groupRepository.save(createdGroup);
        groupMemberRepository.save(groupMember);
    }


    @Override
    public void editGroup(Long groupId, RequestGroupDto requestGroupDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER
                || groupMember.getGroupPermissionType() == GroupPermissionType.MODERATOR) {
            throw new ForbiddenException("Group member or moderator does not have access to edit group information");
        }

        if (group.getImage() != null) {
            String lastImageId = group.getImage().getImageId();
            group.setImage(null);
            imageRepository.deleteByImageId(lastImageId);
        }

        if (imageFile != null) {
            Image updatedImages = fileService.storageOneImage(imageFile, group.getGroupCreator(), false);
            group.setImage(updatedImages);
        }

        group.setName(requestGroupDto.getName());
        group.setDescription(requestGroupDto.getDescription());
        group.setPublic(Boolean.parseBoolean(requestGroupDto.getIsPublic()));

        groupRepository.save(group);
    }

    @Override
    public void deleteGroupById(Long groupId, boolean archive) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR) {
            throw new ForbiddenException("Only the administrator can delete a group");
        }

        if (archive) {
            group.setDeleted(true);
            groupRepository.save(group);
        } else {
            groupRepository.deleteByGroupId(groupId);
        }
    }

    @Override
    public List<GroupDto> findAllGroups(boolean isPublic) {
        List<Group> groups = groupRepository.findByIsDeletedAndIsPublicOrderByCreatedAtDesc(false, isPublic);
        return groupDtoListMapper.convert(groups);
    }

    @Override
    public GroupDetailsDto findGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));
        GroupDetailsDto groupDetailsDto = groupDetailsDtoMapper.convert(group);
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupAndGroupMemberStatus(group, GroupMemberStatus.JOINED);
        groupDetailsDto.setMembers(groupMemberDtoListMapper.convert(groupMembers));

        return groupDetailsDto;
    }

    @Override
    public void addGroupRuleByGroupId(Long groupId, RequestGroupRuleDto requestGroupRuleDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER) {
            throw new ForbiddenException("Group member does not have access to add rule");
        }

        GroupRule newGroupRule = groupRuleMapper.convert(requestGroupRuleDto);
        newGroupRule.setGroup(group);

        groupRuleRepository.save(newGroupRule);
    }

    @Override
    public void editGroupRuleByGroupId(Long groupId, Long ruleId, RequestGroupRuleDto requestGroupRuleDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER) {
            throw new ForbiddenException("Group member does not have access to edit rule");
        }

        GroupRule editedGroupRule = groupRuleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("Not found group rule with id: " + ruleId));

        editedGroupRule.setName(requestGroupRuleDto.getName());
        editedGroupRule.setDescription(requestGroupRuleDto.getDescription());

        groupRuleRepository.save(editedGroupRule);
    }

    @Override
    public void deleteGroupRuleByGroupId(Long groupId, Long ruleId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER) {
            throw new ForbiddenException("Group member does not have access to edit rule");
        }

        groupRuleRepository.deleteByRuleId(ruleId);
    }

    @Override
    public List<InterestDto> findAllInterests() {
        return interestDtoListMapper.convert(interestRepository.findAll());
    }

    @Override
    public void addGroupInterest(Long groupId, Long interestId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER) {
            throw new ForbiddenException("Group member does not have access to add interests");
        }

        Interest addedInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new NotFoundException("Not found interest with id: " + interestId));
        Set<Interest> groupInterests = group.getGroupInterests();
        if (groupInterests.contains(addedInterest)) {
            throw new ConflictRequestException("The given interest has already been added to the group's interests");
        }
        groupInterests.add(addedInterest);
        group.setGroupInterests(groupInterests);
        groupRepository.save(group);
    }

    @Override
    public void deleteGroupInterest(Long groupId, Long interestId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER) {
            throw new ForbiddenException("Group member does not have access to delete interests");
        }

        Interest deletedInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new NotFoundException("Not found interest with id: " + interestId));
        Set<Interest> groupInterests = group.getGroupInterests();
        if (!groupInterests.contains(deletedInterest)) {
            throw new BadRequestException("This group does not contain this interest");
        }

        groupInterests.remove(deletedInterest);
        group.setGroupInterests(groupInterests);
        groupRepository.save(group);
    }

    @Override
    public void inviteUserToGroup(Long groupId, Long invitedUserId) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember loggedUserMember = groupMemberRepository.findByGroupAndMember(group, loggedUser)
                .orElseThrow(() -> new NotFoundException("Logged user is not member of the group with id: " + groupId));

        if (loggedUserMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                && loggedUserMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT) {
            throw new ForbiddenException("Logged user not have access to invite in group with id: " + groupId);
        }

        User user = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + invitedUserId));

        if (groupMemberRepository.existsByMemberAndGroup(user, group)) {
            throw new ConflictRequestException("The user is already a member of the group or an invitation has already been sent to him");
        }

        GroupMember groupMember = GroupMember.builder()
                .groupPermissionType(null)
                .groupMemberStatus(GroupMemberStatus.INVITED)
                .invitationDate(LocalDateTime.now())
                .invitationDisplayed(false)
                .member(user)
                .group(group)
                .build();

        groupMemberRepository.save(groupMember);
    }

    @Override
    public List<GroupInvitationDto> findAllUserGroupInvitations(boolean isDisplayed) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<GroupMember> groupMembers = groupMemberRepository.findByMemberAndGroupMemberStatus(user, GroupMemberStatus.INVITED);
        if (isDisplayed) {
            groupMemberRepository.setGroupInvitationDisplayed(true, user);
        }
        return groupInvitationDtoListMapper.convert(groupMembers);
    }

    @Override
    public void respondToGroupInvitation(Long groupId, boolean isInvitationAccepted) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        if (groupMember.getGroupMemberStatus() != GroupMemberStatus.INVITED) {
            throw new ConflictRequestException("The user is already a member of the group or declined the invitation");
        }

        if (isInvitationAccepted) {
            groupMember.setGroupMemberStatus(GroupMemberStatus.JOINED);
            groupMember.setAddedIn(LocalDateTime.now());
        } else {
            groupMember.setGroupMemberStatus(GroupMemberStatus.REJECTED);
        }

        groupMemberRepository.save(groupMember);
    }

    @Override
    public List<PostDto> findAllGroupPostsById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));
        List<Post> groupPosts = Lists.newArrayList(group.getPosts());
        return postDtoListMapper.convert(groupPosts);
    }

    @Override
    public void addGroupThread(Long groupId, RequestThreadDto requestThreadDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

        GroupThread groupThread = groupThreadMapper.convert(requestThreadDto);
        groupThread.setGroup(group);
        groupThread.setThreadAuthor(groupMember);

        if (imageFile != null) {
            Image image = fileService.storageOneImage(imageFile, user, false);
            groupThread.setImage(image);
        }

        groupThreadRepository.save(groupThread);
    }

    @Override
    public void editGroupThreadById(Long threadId, RequestThreadDto requestThreadDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        GroupThread groupThread = groupThreadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Not found group thread with id: " + threadId));

        if (groupThread.getThreadAuthor().getMember() != user) {
            throw new ForbiddenException("Invalid group thread author - thread editing access forbidden");
        }

        if (groupThread.getImage() != null) {
            String lastImageId = groupThread.getImage().getImageId();
            groupThread.setImage(null);
            imageRepository.deleteByImageId(lastImageId);
        }

        if (imageFile != null) {
            Image updatedImages = fileService.storageOneImage(imageFile, user, false);
            groupThread.setImage(updatedImages);
        }

        groupThread.setTitle(requestThreadDto.getTitle());
        groupThread.setContent(requestThreadDto.getContent());

        groupThreadRepository.save(groupThread);
    }

    @Override
    public void deleteGroupThreadById(Long threadId) {
        Long userId = jwtUtils.getLoggedUserId();
        GroupThread groupThread = groupThreadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Not found group thread with id: " + threadId));

        if (!groupThread.getThreadAuthor().getMember().getUserId().equals(userId)) {
            throw new ForbiddenException("Invalid group thread author id - thread deleting access forbidden");
        }

        groupThreadRepository.deleteByThreadId(threadId);
    }

    @Override
    public void addThreadAnswer(Long threadId, RequestThreadAnswerDto requestThreadAnswerDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        GroupThread groupThread = groupThreadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Not found group thread with id: " + threadId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(groupThread.getGroup(), user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                        userId, groupThread.getGroup().getGroupId())));

        ThreadAnswer threadAnswer = ThreadAnswer.builder()
                .text(requestThreadAnswerDto.getText())
                .date(LocalDateTime.now())
                .answerAuthor(groupMember)
                .groupThread(groupThread)
                .build();
        threadAnswerRepository.save(threadAnswer);
    }

    @Override
    public void editThreadAnswerById(Long answerId, RequestThreadAnswerDto requestThreadAnswerDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ThreadAnswer threadAnswer = threadAnswerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Not found thread answer with id: " + answerId));

        if (threadAnswer.getAnswerAuthor().getMember() != user) {
            throw new ForbiddenException("Invalid group thread answer author - answer editing access forbidden");
        }

        threadAnswer.setText(requestThreadAnswerDto.getText());
        threadAnswerRepository.save(threadAnswer);
    }

    @Override
    public void deleteThreadAnswerById(Long answerId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ThreadAnswer threadAnswer = threadAnswerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Not found thread answer with id: " + answerId));

        if (threadAnswer.getAnswerAuthor().getMember() != user) {
            throw new ForbiddenException("Invalid group thread answer author - answer deleting access forbidden");
        }

        threadAnswerRepository.deleteByAnswerId(answerId);
    }

    @Override
    public void addThreadAnswerReview(Long answerId, RequestThreadAnswerReviewDto requestThreadAnswerReviewDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ThreadAnswer threadAnswer = threadAnswerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Not found group thread answer with id: " + answerId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(threadAnswer.getGroupThread().getGroup(), user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                        userId, threadAnswer.getGroupThread().getGroup().getGroupId())));

        if (threadAnswerReviewRepository.existsByThreadAnswerAndAnswerReviewAuthor(threadAnswer, groupMember)) {
            throw new ConflictRequestException("The user has already rated this thread answer");
        }

        ThreadAnswerReview threadAnswerReview = ThreadAnswerReview.builder()
                .threadAnswer(threadAnswer)
                .answerReviewAuthor(groupMember)
                .rate(requestThreadAnswerReviewDto.getRate())
                .date(LocalDateTime.now())
                .build();

        List<Float> answerRates = threadAnswer.getReviews().stream().map(ThreadAnswerReview::getRate)
                .collect(Collectors.toList());

        double lastRatesSum = answerRates.stream()
                .mapToDouble(Float::doubleValue)
                .sum();

        threadAnswer.setAverageRate((float) ((lastRatesSum + requestThreadAnswerReviewDto.getRate()) / (answerRates.size() + 1)));

        threadAnswerRepository.save(threadAnswer);
        threadAnswerReviewRepository.save(threadAnswerReview);
    }

    @Override
    public void editThreadAnswerReviewById(Long reviewId, RequestThreadAnswerReviewDto requestThreadAnswerReviewDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ThreadAnswerReview threadAnswerReview = threadAnswerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Not found group thread answer review with id: " + reviewId));

        if (threadAnswerReview.getAnswerReviewAuthor().getMember() != user) {
            throw new ForbiddenException("Invalid group thread answer review author - review editing access forbidden");
        }

        threadAnswerReview.setRate(requestThreadAnswerReviewDto.getRate());
        threadAnswerReviewRepository.save(threadAnswerReview);
    }

    @Override
    public void deleteThreadAnswerReviewById(Long reviewId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ThreadAnswerReview threadAnswerReview = threadAnswerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Not found group thread answer review with id: " + reviewId));

        if (threadAnswerReview.getAnswerReviewAuthor().getMember() != user) {
            throw new ForbiddenException("Invalid group thread answer review author - review deleting access forbidden");
        }

        threadAnswerReviewRepository.deleteByAnswerReviewId(reviewId);
    }

    @Override
    public void wantToJoinGroup(Long groupId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (groupMemberRepository.existsByMemberAndGroup(user, group)) {
            throw new ConflictRequestException("The user is already a member of the group or an invitation has already been sent to him");
        }

        GroupMember groupMember = GroupMember.builder()
                .groupPermissionType(null)
                .groupMemberStatus(GroupMemberStatus.WANT_TO_JOIN)
                .invitationDate(null)
                .invitationDisplayed(false)
                .member(user)
                .group(group)
                .build();

        groupMemberRepository.save(groupMember);
    }

    @Override
    public List<UserDto> findAllUserRequestToJoinGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        List<GroupMember> requestGroupMembers = groupMemberRepository.findByGroupAndGroupMemberStatus(group, GroupMemberStatus.WANT_TO_JOIN);
        List<User> requestUsers = requestGroupMembers.stream().map(GroupMember::getMember).collect(Collectors.toList());
        return userDtoListMapper.convert(requestUsers);
    }

    @Override
    public void decideAboutRequestToJoin(Long groupId, Long requesterId, boolean isApproved) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        GroupMember loggedUserMember = groupMemberRepository.findByGroupAndMember(group, loggedUser)
                .orElseThrow(() -> new NotFoundException("Logged user is not member of the group with id: " + groupId));

        if (loggedUserMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                && loggedUserMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT) {
            throw new ForbiddenException("Logged user not have access to decide about request to join the group with id: " + groupId);
        }

        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requesterId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                        user.getUserId(), group.getGroupId())));

        if (isApproved) {
            groupMember.setGroupMemberStatus(GroupMemberStatus.JOINED);
            groupMember.setGroupPermissionType(GroupPermissionType.MEMBER);
            groupMember.setAddedIn(LocalDateTime.now());
        } else {
            groupMember.setGroupMemberStatus(GroupMemberStatus.REJECTED);
        }

        groupMemberRepository.save(groupMember);
    }

    @Override
    public void setGroupMemberPermission(Long groupId, Long memberId, String permission) {
        List<String> groupPermissionTypeList = Arrays.stream(GroupPermissionType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        if (!groupPermissionTypeList.contains(permission)) {
            throw new BadRequestException("Unknown permission type");
        }

        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (group.getGroupCreator() != user) {
            throw new ForbiddenException("Only the group creator can assign permissions");
        }

        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Not found group member with id: " + memberId));

        member.setGroupPermissionType(GroupPermissionType.valueOf(permission));

        groupMemberRepository.save(member);
    }
}