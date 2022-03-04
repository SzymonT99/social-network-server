package com.server.springboot.service.impl;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.request.RequestGroupDto;
import com.server.springboot.domain.dto.request.RequestGroupRuleDto;
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
import java.util.List;
import java.util.Set;

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
    private final Converter<List<GroupDto>, List<Group>> groupDtoListMapper;
    private final Converter<GroupDetailsDto, Group> groupDetailsDtoMapper;
    private final Converter<GroupRule, RequestGroupRuleDto> groupRuleMapper;
    private final Converter<List<InterestDto>, List<Interest>> interestDtoListMapper;
    private final Converter<List<GroupInvitationDto>, List<GroupMember>> groupInvitationDtoListMapper;
    private final Converter<List<PostDto>, List<Post>> postDtoListMapper;

    @Autowired

    public GroupServiceImpl(GroupRepository groupRepository, GroupRuleRepository groupRuleRepository,
                            GroupMemberRepository groupMemberRepository, UserRepository userRepository,
                            Converter<Group, RequestGroupDto> groupMapper, FileService fileService, JwtUtils jwtUtils,
                            ImageRepository imageRepository, InterestRepository interestRepository,
                            Converter<List<GroupDto>, List<Group>> groupDtoListMapper,
                            Converter<GroupDetailsDto, Group> groupDetailsDtoMapper,
                            Converter<GroupRule, RequestGroupRuleDto> groupRuleMapper,
                            Converter<List<InterestDto>, List<Interest>> interestDtoListMapper,
                            Converter<List<GroupInvitationDto>, List<GroupMember>> groupInvitationDtoListMapper,
                            Converter<List<PostDto>, List<Post>> postDtoListMapper) {
        this.groupRepository = groupRepository;
        this.groupRuleRepository = groupRuleRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupMapper = groupMapper;
        this.fileService = fileService;
        this.jwtUtils = jwtUtils;
        this.imageRepository = imageRepository;
        this.interestRepository = interestRepository;
        this.groupDtoListMapper = groupDtoListMapper;
        this.groupDetailsDtoMapper = groupDetailsDtoMapper;
        this.groupRuleMapper = groupRuleMapper;
        this.interestDtoListMapper = interestDtoListMapper;
        this.groupInvitationDtoListMapper = groupInvitationDtoListMapper;
        this.postDtoListMapper = postDtoListMapper;
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

        groupRepository.save(createdGroup);
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
        return groupDetailsDtoMapper.convert(group);
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
        User user = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + invitedUserId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));

        if (groupMemberRepository.existsByMemberAndGroup(user, group)) {
            throw new ConflictRequestException("The user is already a member of the group or an invitation has already been sent to him");
        }

        GroupMember groupMember = GroupMember.builder()
                .groupPermissionType(GroupPermissionType.MEMBER)
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

        if (groupMemberRepository.existsByMemberAndGroup(user, group)) {
            throw new ConflictRequestException("The user is already a member of the group or declined the invitation");
        }

        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new NotFoundException(String.format("Not found user with id: %s in group with id: %s", userId, groupId)));

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
}

