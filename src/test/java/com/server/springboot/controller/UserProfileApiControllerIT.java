package com.server.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.request.UpdateUserProfileDto;
import com.server.springboot.domain.dto.response.InterestDto;
import com.server.springboot.domain.entity.Interest;
import com.server.springboot.domain.entity.UserProfile;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.enumeration.RelationshipStatus;
import com.server.springboot.domain.mapper.InterestDtoListMapper;
import com.server.springboot.domain.repository.InterestRepository;
import com.server.springboot.domain.repository.UserProfileRepository;
import com.server.springboot.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserProfileApiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestDtoListMapper interestDtoListMapper;

    @Test
    public void shouldGetUserProfileInformation() throws Exception {
        Long userId = 1L;
        UserProfile userProfile = userProfileRepository.findById(userId).get();

        mockMvc.perform(get("/api/profile/{userId}/information", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(userProfile.getUserProfileId()))
                .andExpect(jsonPath("$.isPublic").value(userProfile.isPublic()))
                .andExpect(jsonPath("$.firstName").value(userProfile.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userProfile.getLastName()))
                .andDo(print());
    }

    @Test
    @WithUserDetails("Jan123")
    public void shouldUpdateUserProfileInformation() throws Exception {
        Long userId = 1L;
        UpdateUserProfileDto updateUserProfileDto = UpdateUserProfileDto.builder()
                .firstName("Jan")
                .lastName("Nowak")
                .isPublic(false)
                .gender(Gender.MALE.toString())
                .relationshipStatus(RelationshipStatus.SINGLE.toString())
                .dateOfBirth("1989-01-01")
                .aboutUser("Opis użytkownika")  // edytowano
                .job("Programista")             // edytowano
                .build();

        mockMvc.perform(put("/api/profile/{userId}/information", userId)
                .content(objectMapper.writeValueAsString(updateUserProfileDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        UserProfile userProfile = userProfileRepository.findById(userId).get();

        assertEquals(updateUserProfileDto.getAboutUser(), userProfile.getAboutUser());
        assertEquals(updateUserProfileDto.getJob(), userProfile.getJob());
    }

    @Test
    @Transactional
    @WithUserDetails("Jan123")
    public void shouldGetUserInterests() throws Exception {
        Long userId = 1L;
        Set<Interest> userInterest = userRepository.findById(userId).get().getUserInterests();

        MvcResult mvcResult = mockMvc.perform(get("/api/profile/{userId}/interests", userId))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertEquals(interestDtoListMapper.convert(Lists.newArrayList(userInterest)),
                Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), InterestDto[].class)));

    }

    @Test
    @WithUserDetails("Jan123")
    public void shouldDeleteUserInterests() throws Exception {
        Long userId = 1L;
        Long interestId = 1L;

        mockMvc.perform(get("/api/profile/{userId}/interests/{interestId}", userId, interestId))
                .andExpect(status().isOk())
                .andDo(print());

        Set<Interest> userInterest = userRepository.findById(userId).get().getUserInterests();
        assertEquals(0, userInterest.stream()
                .filter(el -> el.getInterestId().equals(interestId)).count());
    }
}