package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestGroupDto;
import com.server.springboot.domain.dto.response.GroupDetailsDto;
import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.GroupRepository;
import com.server.springboot.domain.repository.ImageRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final Converter<Group, RequestGroupDto> groupMapper;
    private final FileService fileService;
    private final JwtUtils jwtUtils;
    private final ImageRepository imageRepository;
    private final Converter<List<GroupDto>, List<Group>> groupDtoListMapper;
    private final Converter<GroupDetailsDto, Group> groupDetailsDtoMapper;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository,
                            Converter<Group, RequestGroupDto> groupMapper, FileService fileService, JwtUtils jwtUtils,
                            ImageRepository imageRepository, Converter<List<GroupDto>, List<Group>> groupDtoListMapper,
                            Converter<GroupDetailsDto, Group> groupDetailsDtoMapper) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMapper = groupMapper;
        this.fileService = fileService;
        this.jwtUtils = jwtUtils;
        this.imageRepository = imageRepository;
        this.groupDtoListMapper = groupDtoListMapper;
        this.groupDetailsDtoMapper = groupDetailsDtoMapper;
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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));
        if (!group.getGroupCreator().getUserId().equals(userId)) {
            throw new ForbiddenException("Invalid group creator id - group editing access forbidden");
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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Not found group with id: " + groupId));
        if (!group.getGroupCreator().getUserId().equals(userId)) {
            throw new ForbiddenException("Invalid group creator id - group deleting access forbidden");
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
}
