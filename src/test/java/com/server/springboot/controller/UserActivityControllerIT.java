package com.server.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.springboot.domain.dto.response.BoardActivityItemDto;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.ActivityType;
import com.server.springboot.domain.repository.PostRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.service.impl.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserActivityControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    @Transactional
    public void shouldGetUserActivityBoard() throws Exception {
        User user = userRepository.findById(1L).get();
        List<Post> userPosts = postRepository.findAllByPostAuthor(user);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        MvcResult mvcResult = mockMvc.perform(get("/api/activity")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        List<BoardActivityItemDto> userActivityList =
                Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BoardActivityItemDto[].class));
        assertTrue(userActivityList.size() > 0);

        List<BoardActivityItemDto> postActivityList = userActivityList.stream()
                .filter(el -> el.getActivityType() == ActivityType.CREATE_POST)
                .collect(Collectors.toList());
        assertTrue(postActivityList.size() >= userPosts.size());
    }
}
