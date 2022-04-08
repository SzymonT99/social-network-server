package com.server.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.GroupDto;
import com.server.springboot.domain.entity.Friend;
import com.server.springboot.domain.entity.Group;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.mapper.FriendDtoListMapper;
import com.server.springboot.domain.repository.FriendRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.service.impl.UserDetailsImpl;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FriendApiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendDtoListMapper friendDtoListMapper;

    @Test
    public void shouldInviteUserToFriend() throws Exception {
        User currentUser = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        Long invitedUserId = 2L;

        mockMvc.perform(post("/api/friends/invitations")
                .with(user(userDetails))
                .param("userId", invitedUserId.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());

        User invitedUser = userRepository.findById(invitedUserId).get();
        Friend invitedFriend = friendRepository.findByUserAndUserFriend(currentUser, invitedUser).get();
        assertNotNull(invitedFriend.getInvitationDate());
        assertNull(invitedFriend.getIsInvitationAccepted());
        assertNull(invitedFriend.getFriendFromDate());
    }

    @Test
    public void shouldAcceptFriendInvitation() throws Exception {
        User currentUser = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        Long inviterId = 2L;

        mockMvc.perform(put("/api/friends/{inviterId}/response", inviterId)
                .with(user(userDetails))
                .param("reaction", "accept"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());

        User inviter = userRepository.findById(inviterId).get();
        Friend newFriend = friendRepository.findByUserAndUserFriend(currentUser, inviter).get();

        assertTrue(newFriend.getIsInvitationAccepted());
        assertNotNull(newFriend.getFriendFromDate());
    }

    @Test
    public void shouldDeleteUserFromFriend() throws Exception {
        User currentUser = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);

        Long friendId = 1L;
        Friend friend = friendRepository.findById(friendId).get();
        User userFriend = friend.getUserFriend();

        mockMvc.perform(delete("/api/friends/{friendId}", friendId)
                .with(user(userDetails))
                .param("isDeletedInvitation", "false"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());

        assertFalse(friendRepository.existsByUserAndUserFriend(currentUser, userFriend));
        assertFalse(friendRepository.existsByUserAndUserFriend(userFriend, currentUser));
    }

    @Test
    public void shouldGetAllUserFriends() throws Exception {
        User currentUser = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);

        List<Friend> userFriends = Lists.newArrayList(currentUser.getUserFriends());

        MvcResult mvcResult = mockMvc.perform(post("/api/friends")
                .with(user(userDetails)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.size()").value(userFriends.size()))
                .andDo(print())
                .andReturn();

        assertEquals(friendDtoListMapper.convert(userFriends),
                Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), FriendDto[].class)));
    }

    @Test
    public void shouldGetAllUserFriendSuggestions() throws Exception {
        User currentUser = userRepository.findById(1L).get();
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);

        mockMvc.perform(get("/api/friends/suggestions")
                .with(user(userDetails)))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }
}
