package com.server.springboot.service.impl;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.enumeration.GroupPermissionType;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.*;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.GroupService;
import com.server.springboot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupRuleRepository groupRuleRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final FileService fileService;
    private final JwtUtils jwtUtils;
    private final ImageRepository imageRepository;
    private final InterestRepository interestRepository;
    private final GroupThreadRepository groupThreadRepository;
    private final ThreadAnswerRepository threadAnswerRepository;
    private final ThreadAnswerReviewRepository threadAnswerReviewRepository;
    private final GroupDtoListMapper groupDtoListMapper;
    private final GroupDetailsDtoMapper groupDetailsDtoMapper;
    private final GroupRuleMapper groupRuleMapper;
    private final GroupInvitationDtoListMapper groupInvitationDtoListMapper;
    private final PostDtoListMapper postDtoListMapper;
    private final ThreadMapper groupThreadMapper;
    private final UserDtoMapper userDtoMapper;
    private final UserDtoListMapper userDtoListMapper;
    private final GroupMemberDtoListMapper groupMemberDtoListMapper;
    private final GroupThreadDtoListMapper groupThreadDtoListMapper;
    private final NotificationService notificationService;
    private final RoleRepository roleRepository;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, GroupRuleRepository groupRuleRepository,
                            GroupMemberRepository groupMemberRepository, UserRepository userRepository,
                            GroupMapper groupMapper, FileService fileService, JwtUtils jwtUtils,
                            ImageRepository imageRepository, InterestRepository interestRepository,
                            GroupThreadRepository groupThreadRepository,
                            ThreadAnswerRepository threadAnswerRepository,
                            ThreadAnswerReviewRepository threadAnswerReviewRepository,
                            GroupDtoListMapper groupDtoListMapper, GroupDetailsDtoMapper groupDetailsDtoMapper,
                            GroupRuleMapper groupRuleMapper, GroupInvitationDtoListMapper groupInvitationDtoListMapper,
                            PostDtoListMapper postDtoListMapper, ThreadMapper groupThreadMapper,
                            UserDtoMapper userDtoMapper, UserDtoListMapper userDtoListMapper,
                            GroupMemberDtoListMapper groupMemberDtoListMapper,
                            GroupThreadDtoListMapper groupThreadDtoListMapper,
                            NotificationService notificationService, RoleRepository roleRepository) {
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
        this.groupInvitationDtoListMapper = groupInvitationDtoListMapper;
        this.postDtoListMapper = postDtoListMapper;
        this.groupThreadMapper = groupThreadMapper;
        this.userDtoMapper = userDtoMapper;
        this.userDtoListMapper = userDtoListMapper;
        this.groupMemberDtoListMapper = groupMemberDtoListMapper;
        this.groupThreadDtoListMapper = groupThreadDtoListMapper;
        this.notificationService = notificationService;
        this.roleRepository = roleRepository;
    }

    @Override
    public GroupDetailsDto createGroup(RequestGroupDto requestGroupDto, MultipartFile imageFile) {
        Long userId = jwtUtils.getLoggedUserId();
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + (userId)));

        Group createdGroup = groupMapper.convert(requestGroupDto);
        createdGroup.setGroupCreator(creator);
        createdGroup.setGroupInterests(requestGroupDto.getInterests().stream()
                .map(interest -> Interest.builder()
                        .interestId(interest.getInterestId())
                        .name(interest.getName())
                        .build())
                .collect(Collectors.toSet()));

        if (imageFile != null) {
            Image image = fileService.storageOneImage(imageFile, creator, false);
            createdGroup.setImage(image);
        }

        groupRepository.save(createdGroup);

        GroupMember groupMember = GroupMember.builder()
                .groupPermissionType(GroupPermissionType.ADMINISTRATOR)
                .groupMemberStatus(GroupMemberStatus.JOINED)
                .addedIn(LocalDateTime.now())
                .member(creator)
                .group(createdGroup)
                .build();
        groupMemberRepository.save(groupMember);

        return groupDetailsDtoMapper.convert(createdGroup);
    }


    @Override
    public void editGroup(Long groupId, RequestGroupDto requestGroupDto, MultipartFile imageFile) throws IOException {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER
                    || groupMember.getGroupPermissionType() == GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit group information");
            }
        }

        if (group.getImage() != null) {
            String lastImageId = group.getImage().getImageId();
            group.setImage(null);
            imageRepository.deleteByImageId(lastImageId);
            fileService.deleteImage(group.getImage().getImageId());
        }

        if (imageFile != null) {
            Image updatedImages = fileService.storageOneImage(imageFile, group.getGroupCreator(), false);
            group.setImage(updatedImages);
        }

        group.setName(requestGroupDto.getName());
        group.setDescription(requestGroupDto.getDescription());
        group.setPublic(Boolean.parseBoolean(requestGroupDto.getIsPublic()));
        group.setGroupInterests(requestGroupDto.getInterests().stream()
                .map(interest -> Interest.builder()
                        .interestId(interest.getInterestId())
                        .name(interest.getName())
                        .build())
                .collect(Collectors.toSet()));

        groupRepository.save(group);
    }

    @Override
    public void deleteGroupById(Long groupId, boolean archive) throws IOException {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (groupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR) {
                throw new ForbiddenException("Only the administrator can delete a group");
            }
        }

        if (archive) {
            group.setDeleted(true);
            groupRepository.save(group);
        } else {
            groupRepository.deleteByGroupId(groupId);
            if (group.getImage() != null) {
                fileService.deleteImage(group.getImage().getImageId());
            }
        }
    }

    @Override
    public List<GroupDto> findAllGroups(boolean arePublic) {
        List<Group> groups = new ArrayList<>();
        if (arePublic) {
            groups = groupRepository.findByIsDeletedAndIsPublicOrderByCreatedAtDesc(false, true);
        } else {
            groups = groupRepository.findByIsDeletedOrderByCreatedAtDesc(false);
        }
        return groupDtoListMapper.convert(groups);
    }

    @Override
    public List<GroupDto> findAllGroupsWithSimilarInterests() {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        List<Group> userGroups = user.getMemberOfGroups().stream()
                .map(GroupMember::getGroup)
                .collect(Collectors.toList());

        List<Interest> userInterests = Lists.newArrayList(user.getUserInterests());

        List<Group> groups = groupRepository.findByGroupInterestsInAndIsDeletedAndIsPublicOrderByCreatedAtDesc(userInterests, false, true);
        List<Group> filteredGroups = groups.stream()
                .filter((group -> !userGroups.contains(group)))
                .collect(Collectors.toList());
        return groupDtoListMapper.convert(filteredGroups);
    }

    @Override
    public GroupDetailsDto findGroup(Long groupId, boolean onlyPublic) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (group.isDeleted()) {
            throw new ForbiddenException("The group has been deleted and is archived");
        }

        if (onlyPublic && !group.isPublic()) {
            throw new ForbiddenException("No access to private group");
        }

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

        if (!user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER
                    || groupMember.getGroupPermissionType() == GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit group information");
            }
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


        if (!user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER
                    || groupMember.getGroupPermissionType() == GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit group information");
            }
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

        if (!user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER
                    || groupMember.getGroupPermissionType() == GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit group information");
            }
        }

        groupRuleRepository.deleteByRuleId(ruleId);
    }

    @Override
    public void addGroupInterest(Long groupId, Long interestId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER) {
                throw new ForbiddenException("No access to add interests");
            }
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

        if (!user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (groupMember.getGroupPermissionType() == GroupPermissionType.MEMBER
                    && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
                throw new ForbiddenException("No access to delete interests");
            }
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

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            GroupMember loggedUserMember = groupMemberRepository.findByGroupAndMember(group, loggedUser)
                    .orElseThrow(() -> new NotFoundException(
                            "Logged in user is not a member of the group with id: " + groupId));

            if (loggedUserMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && loggedUserMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT) {
                throw new ForbiddenException("Group member or moderator does not have access to invite users to group with id: " + groupId);
            }
        }

        User user = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + invitedUserId));

        if (groupMemberRepository.existsByMemberAndGroup(user, group)) {
            if (groupMemberRepository.findByGroupAndMember(group, user).get().getGroupMemberStatus() == GroupMemberStatus.DELETED) {
                throw new ResourceGoneException("The user has previously been removed from this group");
            } else {
                throw new ConflictRequestException("The user is already a member of the group or an invitation has already been sent to him");
            }
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

        notificationService.sendNotificationToUser(loggedUser, invitedUserId, ActionType.ACTIVITY_BOARD);
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

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user).get();

        if (groupMember.getGroupMemberStatus() != GroupMemberStatus.INVITED) {
            throw new ConflictRequestException("The user is already a member of the group or declined the invitation");
        }

        if (isInvitationAccepted) {
            groupMember.setGroupMemberStatus(GroupMemberStatus.JOINED);
            groupMember.setGroupPermissionType(GroupPermissionType.MEMBER);
            groupMember.setAddedIn(LocalDateTime.now());
            groupMember.setHasNotification(false);
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
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        GroupThread groupThread = groupThreadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Not found group thread with id: " + threadId));

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(groupThread.getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, groupThread.getGroup().getGroupId())));

            if (groupThread.getThreadAuthor().getMember() != loggedUser
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit the thread");
            }
        }

        if (groupThread.getImage() != null) {
            String lastImageId = groupThread.getImage().getImageId();
            groupThread.setImage(null);
            imageRepository.deleteByImageId(lastImageId);
        }

        if (imageFile != null) {
            Image updatedImages = fileService.storageOneImage(imageFile, loggedUser, false);
            groupThread.setImage(updatedImages);
        }

        groupThread.setEdited(true);
        groupThread.setTitle(requestThreadDto.getTitle());
        groupThread.setContent(requestThreadDto.getContent());

        groupThreadRepository.save(groupThread);
    }

    @Override
    public void deleteGroupThreadById(Long threadId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        GroupThread groupThread = groupThreadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Not found group thread with id: " + threadId));

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(groupThread.getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, groupThread.getGroup().getGroupId())));

            if (groupThread.getThreadAuthor().getMember() != loggedUser
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to delete the thread");
            }
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
                .isEdited(false)
                .date(LocalDateTime.now())
                .answerAuthor(groupMember)
                .groupThread(groupThread)
                .build();
        threadAnswerRepository.save(threadAnswer);
    }

    @Override
    public void editThreadAnswerById(Long answerId, RequestThreadAnswerDto requestThreadAnswerDto) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ThreadAnswer threadAnswer = threadAnswerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Not found thread answer with id: " + answerId));

        if (threadAnswer.getAnswerAuthor().getMember() != loggedUser
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(threadAnswer.getGroupThread().getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, threadAnswer.getGroupThread().getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to edit the thread answer");
            }
        }

        threadAnswer.setEdited(true);
        threadAnswer.setText(requestThreadAnswerDto.getText());
        threadAnswerRepository.save(threadAnswer);
    }

    @Override
    public void deleteThreadAnswerById(Long answerId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        ThreadAnswer threadAnswer = threadAnswerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Not found thread answer with id: " + answerId));

        if (threadAnswer.getAnswerAuthor().getMember() != loggedUser
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            GroupMember userGroupMember = groupMemberRepository.findByGroupAndMember(threadAnswer.getGroupThread().getGroup(), loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            userId, threadAnswer.getGroupThread().getGroup().getGroupId())));
            if (userGroupMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT
                    && userGroupMember.getGroupPermissionType() != GroupPermissionType.MODERATOR) {
                throw new ForbiddenException("No access to delete the thread answer");
            }
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

        threadAnswer.setAverageRating((float) ((lastRatesSum + requestThreadAnswerReviewDto.getRate()) / (answerRates.size() + 1)));

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
            throw new ForbiddenException("Review editing access forbidden");
        }

        ThreadAnswer threadAnswer = threadAnswerReview.getThreadAnswer();

        List<Float> answerRates = threadAnswer.getReviews()
                .stream()
                .filter(review -> !review.getAnswerReviewId().equals(reviewId))
                .map(ThreadAnswerReview::getRate)
                .collect(Collectors.toList());

        double lastRatesSum = answerRates.stream()
                .mapToDouble(Float::doubleValue)
                .sum();

        threadAnswer.setAverageRating((float) ((lastRatesSum + requestThreadAnswerReviewDto.getRate()) / (answerRates.size() + 1)));

        threadAnswerReview.setRate(requestThreadAnswerReviewDto.getRate());

        threadAnswerRepository.save(threadAnswer);
        threadAnswerReviewRepository.save(threadAnswerReview);
    }

    @Override
    public void wantToJoinGroup(Long groupId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (groupMemberRepository.existsByMemberAndGroup(user, group)) {
            if (groupMemberRepository.findByGroupAndMember(group, user).get().getGroupMemberStatus() == GroupMemberStatus.DELETED) {
                throw new ResourceGoneException("The user has previously been removed from this group");
            } else {
                throw new ConflictRequestException("The user is already a member of the group or an invitation has already been sent to him");
            }
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

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            GroupMember loggedUserMember = groupMemberRepository.findByGroupAndMember(group, loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            loggedUser, group.getGroupId())));

            if (loggedUserMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && loggedUserMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT) {
                throw new ForbiddenException("No access to decide about join request in group with id: " + groupId);
            }
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + requesterId));

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, requester).get();

        if (isApproved) {
            groupMember.setGroupMemberStatus(GroupMemberStatus.JOINED);
            groupMember.setGroupPermissionType(GroupPermissionType.MEMBER);
            groupMember.setAddedIn(LocalDateTime.now());
            groupMember.setHasNotification(true);
        } else {
            groupMember.setGroupMemberStatus(GroupMemberStatus.REJECTED);
        }

        groupMemberRepository.save(groupMember);

        notificationService.sendNotificationToUser(loggedUser, requesterId, ActionType.ACTIVITY_BOARD);
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
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            GroupMember loggedUserMember = groupMemberRepository.findByGroupAndMember(group, loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            loggedUser, group.getGroupId())));

            if (loggedUserMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR) {
                throw new ForbiddenException("Only administrator has access to set permission type in group with id:" + groupId);
            }
        }

        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Not found group member with id: " + memberId));

        member.setGroupPermissionType(GroupPermissionType.valueOf(permission));

        groupMemberRepository.save(member);
    }

    @Override
    public List<GroupDto> findAllUserGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        List<Group> userGroups = groupMemberRepository.findByMemberAndGroupMemberStatus(user, GroupMemberStatus.JOINED).stream()
                .map(GroupMember::getGroup)
                .collect(Collectors.toList());

        return groupDtoListMapper.convert(userGroups);
    }

    @Override
    public void leaveGroupByUser(Long groupId) {
        Long userId = jwtUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!groupMemberRepository.existsByMemberAndGroup(user, group)) {
            throw new NotFoundException("Logged user is not member of the group with id: " + groupId);
        }

        groupMemberRepository.deleteByMemberAndGroup(user, group);
    }

    @Override
    public void deleteGroupMemberById(Long memberId, Long groupId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {

            GroupMember loggedUserMember = groupMemberRepository.findByGroupAndMember(group, loggedUser)
                    .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s",
                            loggedUser, group.getGroupId())));

            if (loggedUserMember.getGroupPermissionType() != GroupPermissionType.ADMINISTRATOR
                    && loggedUserMember.getGroupPermissionType() != GroupPermissionType.ASSISTANT) {
                throw new ForbiddenException("No access to delete user from group with id: " + groupId);
            }
        }

        GroupMember deletedMember = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Not found group member with id: " + memberId));

        deletedMember.setGroupMemberStatus(GroupMemberStatus.DELETED);
        groupMemberRepository.save(deletedMember);
    }

    @Override
    public List<GroupMemberForumStatsDto> getGroupForumStatsById(Long groupId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!groupMemberRepository.existsByMemberAndGroup(loggedUser, group)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new NotFoundException("Logged user is not member of the group with id: " + groupId);
        }

        List<GroupMemberForumStatsDto> forumStatsList = new ArrayList<>();

        List<GroupMember> groupMembers = groupMemberRepository.findByGroupAndGroupMemberStatus(group, GroupMemberStatus.JOINED);

        for (GroupMember member : groupMembers) {

            List<Float> answerRatings = member.getThreadAnswers().stream()
                    .map(ThreadAnswer::getAverageRating)
                    .collect(Collectors.toList());

            answerRatings.removeIf(Objects::isNull);

            GroupMemberForumStatsDto groupMemberForumStats = GroupMemberForumStatsDto.builder()
                    .groupMemberId(member.getGroupMemberId())
                    .user(userDtoMapper.convert(member.getMember()))
                    .threadsNumber(member.getGroupThreads().size())
                    .answersNumber(member.getThreadAnswers().size())
                    .answersAverageRating(answerRatings.size() != 0
                            ? (float) answerRatings.stream().mapToDouble(Float::doubleValue).sum() / answerRatings.size() : 0F)
                    .build();

            forumStatsList.add(groupMemberForumStats);
        }
        return forumStatsList;
    }

    @Override
    public List<GroupThreadDto> findGroupThreadsById(Long groupId) {
        Long userId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (!groupMemberRepository.existsByMemberAndGroup(loggedUser, group)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new NotFoundException("Logged user is not member of the group with id: " + groupId);
        }

        List<GroupThread> groupThreads = Lists.newArrayList(group.getGroupThreads());
        return groupThreadDtoListMapper.convert(groupThreads);
    }
}