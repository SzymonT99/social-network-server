package com.server.springboot.service;

import com.server.springboot.domain.dto.request.RequestGroupDto;
import com.server.springboot.domain.dto.response.GroupDetailsDto;
import com.server.springboot.domain.dto.response.GroupDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupService {

    void addGroup(RequestGroupDto requestGroupDto, MultipartFile imageFile);

    void editGroup(Long groupId, RequestGroupDto requestGroupDto, MultipartFile imageFile);

    void deleteGroupById(Long groupId, boolean archive);

    List<GroupDto> findAllGroups(boolean isPublic);

    GroupDetailsDto findGroup(Long groupId);
}
