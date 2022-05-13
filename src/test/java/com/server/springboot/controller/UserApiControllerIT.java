package com.server.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.enumeration.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserApiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldCreateUserAccount() throws Exception {
        CreateUserDto createUserDto = CreateUserDto.builder()
                .firstName("Piotr")
                .lastName("Kowalski")
                .username("Piotr123")
                .phoneNumber("123456789")
                .email("piotrKowalski@gmail.com")
                .password("Qwertyuiop")
                .gender(Gender.MALE)
                .dateOfBirth("1989-01-05")
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/auth/register")
                    .content(objectMapper.writeValueAsString(createUserDto))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void shouldActivateUserAccount() throws Exception {
        String activationToken = "cb83b60f-ea3e-45f0-8b27-ca26b9728128";
        String userEmail = "janNowak@gmail.com";

        mockMvc.perform(put("/api/auth/account-activation")
                .param("token", activationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value(userEmail))
                .andDo(print());
    }

    @Test
    public void shouldAuthenticateUser() throws Exception {
        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("janNowak@gmail.com")
                .password("Qwertyuiop")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(userLoginDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andDo(print());
    }

    @Test
    public void shouldNotAuthenticateUserWhenPasswordIsWrong() throws Exception {
        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("janNowak@gmail.com")
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(userLoginDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void shouldChangeUsername() throws Exception {
        Long userId = 1L;
        ChangeUsernameDto changeUsernameDto = ChangeUsernameDto.builder()
                .oldUsername("Jan123")
                .newUsername("Jan321")
                .password("Qwertyuiop")
                .build();

        mockMvc.perform(put("/api/users/{userId}/username", userId)
                .with(user("Jan123"))
                .content(objectMapper.writeValueAsString(changeUsernameDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andDo(print());
    }

    @Test
    public void shouldChangePassword() throws Exception {
        Long userId = 1L;
        ChangeUserPasswordDto changeUserPasswordDto = ChangeUserPasswordDto.builder()
                .oldPassword("Qwertyuiop")
                .newPassword("newPassword")
                .repeatedNewPassword("newPassword")
                .build();

        mockMvc.perform(put("/api/users/{userId}/password", userId)
                .content(objectMapper.writeValueAsString(changeUserPasswordDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andDo(print());
    }

    @Test
    public void shouldHasNotAccessToMangeUserAccount() throws Exception {
        Long editedUserId = 2L;
        UserAccountUpdateDto userAccountUpdateDto = UserAccountUpdateDto.builder()
                .firstName("Piotr")
                .lastName("Kowalski")
                .username("Piotr123")
                .phoneNumber("123456789")
                .email("piotrKowalski@gmail.com")
                .isPublicProfile(true)
                .incorrectLoginCounter(0)
                .activateAccount(true)
                .isBanned(true)     // Próba ukarania użytkownika
                .isBlocked(false)
                .build();

        mockMvc.perform(put("/api/users/{userId}/accounts", editedUserId)
                .with(user("Jan123").password("Qwertyuiop").roles("USER"))  // brak roli ADMIN
                .content(objectMapper.writeValueAsString(userAccountUpdateDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }
}
