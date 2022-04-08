package com.server.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.request.UpdateUserProfileDto;
import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.dto.response.InterestDto;
import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.entity.UserProfile;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.enumeration.RelationshipStatus;
import com.server.springboot.domain.mapper.GroupDtoListMapper;
import com.server.springboot.domain.repository.GroupMemberRepository;
import com.server.springboot.domain.repository.GroupRepository;
import com.server.springboot.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GroupApiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupDtoListMapper groupDtoListMapper;

    @Test
    @WithUserDetails("Jan123")
    public void shouldGetAllGroups() throws Exception {

        List<Group> publicGroups = groupRepository.findByIsDeletedAndIsPublicOrderByCreatedAtDesc(false, true);

        MvcResult mvcResult = mockMvc.perform(get("/api/groups")
                .param("arePublic", "true"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.size()").value(publicGroups.size()))
                .andDo(print())
                .andReturn();

        assertEquals(groupDtoListMapper.convert(publicGroups),
                Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GroupDto[].class)));
    }

    @Test
    @WithUserDetails("Jan123")
    public void shouldGetGroupDetails() throws Exception {
        Long groupId = 1L;
        Group group = groupRepository.findById(groupId).get();

        mockMvc.perform(get("/api/groups/{groupId}", groupId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.groupId").value(group.getGroupId()))
                .andExpect(jsonPath("$.name").value(group.getName()))
                .andExpect(jsonPath("$.description").value(group.getDescription()))
                .andExpect(jsonPath("$.posts").value(group.getPosts()))
                .andDo(print());
    }

    @Test
    @WithUserDetails("Jan123")
    public void shouldInviteUserToGroups() throws Exception {
        Long groupId = 1L;
        Group group = groupRepository.findById(groupId).get();
        Long invitedUserId = 2L;

        mockMvc.perform(post("/api/groups/{groupId}/invite", groupId)
                .param("invitedUserId", invitedUserId.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());

        User invitedUser = userRepository.findById(invitedUserId).get();
        GroupMember groupMember = groupMemberRepository.findByGroupAndMember(group, invitedUser).get();

        assertEquals(GroupMemberStatus.INVITED, groupMember.getGroupMemberStatus());
    }

    @Test
    @WithUserDetails("Jan123")
    public void shouldDeleteGroupMember() throws Exception {
        Long groupId = 1L;
        Long memberId = 2L;

        mockMvc.perform(post("/api/groups/{groupId}/members/{memberId}", groupId, memberId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());

        assertFalse(groupMemberRepository.existsById(memberId));
    }
}
