package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.UserDetailsDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.repository.ReportRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("Register user")
    @Order(value = 1)
    void addUser() {
        CreateUserDto createUser = CreateUserDto.builder()
                .username("EwaN123")
                .email("ewaNowak1390@domena.com")
                .phoneNumber("123456789")
                .firstName("Ewa")
                .lastName("Nowak")
                .gender(Gender.FEMALE)
                .password("1234567890")
                .dateOfBirth("1989-01-05")
                .build();

        userService.addUser(createUser);

        assertTrue(userRepository.existsByUsername("EwaN123"));
    }

    @Test
    @DisplayName("Activate account")
    @Order(value = 2)
    void activateAccount() {
        String activateToken = "da220146-90e6-4ee4-9069-ae7ff9d6d118";
        userService.activateAccount(activateToken);

        assertTrue(userRepository.findById(12L).get().isVerifiedAccount());
    }

    @Test
    @DisplayName("Authenticate user")
    @Order(value = 3)
    void loginUser() {

        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("EwaN123")
                .password("1234567890122312431")
                .build();

        Exception exception = assertThrows(Exception.class, () -> {
            userService.loginUser(userLoginDto);
        });

        String expectedMessage = "Bad credentials";
        String actualMessage = exception.getMessage();

        assertFalse(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Logout user")
    @Order(value = 4)
    void logoutUser() {

        Long userId = jwtUtils.getLoggedUserId();

        userService.logoutUser(userId);

        assertEquals(ActivityStatus.OFFLINE, userRepository.findById(userId).get().getActivityStatus());
    }

    @Test
    @DisplayName("Delete user")
    @Order(value = 5)
    void deleteUser() {

        DeleteUserDto deleteUserDto = DeleteUserDto.builder()
                .login("EwaN123")
                .password("1234567890")
                .build();

        userService.deleteUser(deleteUserDto, true);

        assertTrue(userRepository.findById(12L).get().isDeleted());
    }

    @Test
    @DisplayName("Change username")
    @Order(value = 6)
    void changeUsername() {

        ChangeUsernameDto changeUsernameDto = ChangeUsernameDto.builder()
                .oldUsername("EwaN123")
                .newUsername("EwaNowak")
                .password("1234567890")
                .build();


        userService.changeUsername(12L, changeUsernameDto);

        assertEquals(userRepository.findById(12L).get().getUsername(), changeUsernameDto.getNewUsername());
    }

    @Test
    @DisplayName("Change username")
    @Order(value = 7)
    void changeEmail() {

        ChangeEmailDto changeEmailDto = ChangeEmailDto.builder()
                .oldEmail("ewaNowak1390@domena.com")
                .newEmail("newEwaNowak1390@domena.com")
                .password("1234567890")
                .build();


        userService.changeEmail(12L, changeEmailDto);

        assertEquals(userRepository.findById(12L).get().getEmail(), changeEmailDto.getNewEmail());
    }

    @Test
    @DisplayName("Change password")
    @Order(value = 8)
    void changePassword() {

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        ChangeUserPasswordDto changeUserPasswordDto = ChangeUserPasswordDto.builder()
                .oldPassword("1234567890")
                .newPassword("qwertyuiop")
                .repeatedNewPassword("qwertyuiop")
                .build();

        userService.changePassword(12L, changeUserPasswordDto);

        assertTrue(bCryptPasswordEncoder.matches(changeUserPasswordDto.getNewPassword(), userRepository.findById(12L).get().getPassword()));
    }

    @Test
    @DisplayName("Change phone number")
    @Order(value = 9)
    void changePhoneNumber() {

        ChangePhoneNumberDto changePhoneNumberDto = ChangePhoneNumberDto.builder()
                .oldPhoneNumber("123456789")
                .newPhoneNumber("987654321")
                .password("1234567890")
                .build();


        userService.changePhoneNumber(12L, changePhoneNumberDto);

        assertEquals(userRepository.findById(12L).get().getPhoneNumber(), changePhoneNumberDto.getNewPhoneNumber());
    }

    @Test
    @DisplayName("Report user")
    @Order(value = 10)
    void reportUserBySuspectId() {

        RequestReportDto requestReportDto = RequestReportDto.builder()
                .suspectId(12L)
                .reportType("RUDE_POST")
                .description("Opublikowano nieodpowiednią treść")
                .build();


        userService.reportUserBySuspectId(requestReportDto);

        assertEquals(reportRepository.findById(1L).get().getSuspect().getUserId(), requestReportDto.getSuspectId());
    }

    @Test
    @DisplayName("Get all users")
    @Order(value = 10)
    void getAllUsers() {

        assertThat(userService.getAllUses())
                .extracting(UserDto::getUserId,UserDto::getEmail)
                .containsExactly(
                        tuple(1L, "w4d354awdsfres3r6546gfhc5634r11gert453g@gmail.com"),
                        tuple(2L, "szy.tyrka@gmail.com"),
                        tuple(3L, "xdrtxdrtdxrtdrxtxdrhbx3ws@gmail.com"),
                        tuple(4L, "janKowalski123@domena.com"),
                        tuple(5L, "janKowalski1234@domena.com"),
                        tuple(6L, "adamK123@domena.com"),
                        tuple(7L, "Ewadeska@domea.com"),
                        tuple(8L, "sandra123@domena.com"),
                        tuple(9L, "qwerty123@domena.com"),
                        tuple(10L, "janNowak1390@domena.com"),
                        tuple(11L, "adamNowak1390@domena.com"),
                        tuple(12L, "ewaNowak1390@domena.com"));

    }


}

