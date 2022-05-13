package com.server.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.GroupMember;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.mapper.GroupDtoListMapper;
import com.server.springboot.domain.mapper.GroupMemberDtoListMapper;
import com.server.springboot.domain.mapper.PostDtoListMapper;
import com.server.springboot.domain.repository.GroupMemberRepository;
import com.server.springboot.domain.repository.GroupRepository;
import com.server.springboot.domain.repository.UserRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private PostDtoListMapper postDtoListMapper;

    @Autowired
    private GroupMemberDtoListMapper memberDtoListMapper;

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
    @Transactional
    public void shouldGetGroupDetails() throws Exception {
        String accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJTenltb25UIiwiZXhwIjoxNjUwNTY2MTI2LCJpYXQiOjE2NTA1NTE3" +
                "MjZ9.IDWbmx66e7WWb2N4NFySKCoecMqvDm8QRew2hQ5QeWjoT391g9_RTgmi2f4FWg4t7Sd3CUGfxDLbNp8EJUQGEg";
        Long groupId = 1L;
        Group group = groupRepository.findById(groupId).get();

        mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/groups/{groupId}", groupId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(group.getGroupId()))
                .andExpect(jsonPath("$.name").value(group.getName()))
                .andExpect(jsonPath("$.description").value(group.getDescription()))
                .andExpect(jsonPath("$.posts").value(postDtoListMapper.convert(Lists.newArrayList(group.getPosts()))))
                .andExpect(jsonPath("$.members").value(memberDtoListMapper.convert(Lists.newArrayList(group.getGroupMembers()))))
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
