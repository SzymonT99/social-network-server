package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.*;

import com.server.springboot.domain.dto.response.ActivatedAccountDto;
import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.domain.dto.response.ReportDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);

    private final UserService userService;

    @Autowired
    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "Register user")
    @PostMapping(value = "/auth/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        LOGGER.info("---- Create user: {}", createUserDto.getUsername());
        userService.addUser(createUserDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Activate account")
    @PutMapping(value = "/auth/account-activation")
    public ResponseEntity<ActivatedAccountDto> confirmUserAccount(@RequestParam("token") String token) {
        LOGGER.info("---- Activate account by token: {}", token);
        return new ResponseEntity<>(userService.activateAccount(token), HttpStatus.OK);
    }

    @ApiOperation(value = "Resend activation account link")
    @PostMapping(value = "/auth/resend-activation")
    public ResponseEntity<?> resendActivationAccountLink(@Email @RequestParam("userEmail") String userEmail) {
        LOGGER.info("---- Resend account user email: {}", userEmail);
        userService.resendActivationLink(userEmail);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Authenticate user")
    @PostMapping(value = "/auth/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody UserLoginDto userLoginDto) {
        LOGGER.info("---- Authenticate user: {}", userLoginDto.getLogin());
        return new ResponseEntity<>(userService.loginUser(userLoginDto), HttpStatus.OK);
    }

    @ApiOperation(value = "Refresh user token")
    @PostMapping(value = "/auth/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LOGGER.info("---- Refresh token");
        String refreshTokenStr = refreshTokenRequest.getRefreshToken();
        return new ResponseEntity<>(userService.refreshExpiredToken(refreshTokenStr), HttpStatus.OK);
    }

    @ApiOperation(value = "Log out user")
    @PutMapping(value = "/auth/logout/{userId}")
    public ResponseEntity<?> logoutUser(@PathVariable(value = "userId") Long userId) {
        userService.logoutUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete user by id")
    @DeleteMapping(value = "/users")
    public ResponseEntity<?> deleteUser(@RequestParam(value = "archive") boolean archive,
                                        @Valid @RequestBody DeleteUserDto deleteUserDto) {
        LOGGER.info("---- Delete user account - archive: {}", archive);
        userService.deleteUser(deleteUserDto, archive);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Change username")
    @PutMapping(value = "/users/{userId}/username")
    public ResponseEntity<JwtResponse> changeUsername(@PathVariable(value = "userId") Long userId,
                                                      @Valid @RequestBody ChangeUsernameDto changeUsernameDto) {
        LOGGER.info("---- User changes username - current username: {} , new username: {}",
                changeUsernameDto.getOldUsername(), changeUsernameDto.getNewUsername());
        return new ResponseEntity<>(userService.changeUsername(userId, changeUsernameDto), HttpStatus.OK);
    }

    @ApiOperation(value = "Change email")
    @PutMapping(value = "/users/{userId}/email")
    public ResponseEntity<JwtResponse> changeEmail(@PathVariable(value = "userId") Long userId,
                                                   @Valid @RequestBody ChangeEmailDto changeEmailDto) {
        LOGGER.info("---- User changes email - current email: {} , new email: {}",
                changeEmailDto.getOldEmail(), changeEmailDto.getNewEmail());
        return new ResponseEntity<>(userService.changeEmail(userId, changeEmailDto), HttpStatus.OK);
    }

    @ApiOperation(value = "Change user password")
    @PutMapping(value = "/users/{userId}/password")
    public ResponseEntity<JwtResponse> changePassword(@PathVariable(value = "userId") Long userId,
                                                      @Valid @RequestBody ChangeUserPasswordDto changeUserPasswordDto) {
        LOGGER.info("---- User changes password");
        return new ResponseEntity<>(userService.changePassword(userId, changeUserPasswordDto), HttpStatus.OK);
    }

    @ApiOperation(value = "Change user phone number")
    @PutMapping(value = "/users/{userId}/phoneNumber")
    public ResponseEntity<?> changePhoneNumber(@PathVariable(value = "userId") Long userId,
                                               @Valid @RequestBody ChangePhoneNumberDto changePhoneNumberDto) {
        LOGGER.info("---- User changes phone number - current phone number: {} , new phone number: {}",
                changePhoneNumberDto.getOldPhoneNumber(), changePhoneNumberDto.getNewPhoneNumber());
        userService.changePhoneNumber(userId, changePhoneNumberDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Send reset user password link")
    @PostMapping(value = "/users/reset-password/step1")
    public ResponseEntity<?> sendResetPasswordLink(@Email @RequestParam(value = "userEmail") String userEmail) {
        LOGGER.info("---- Send reset password link for user with email: {}", userEmail);
        userService.sendResetPasswordLink(userEmail);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Reset not logged user password")
    @PutMapping(value = "/users/reset-password/step2")
    public ResponseEntity<?> resetPasswordNotLoggedUser(@RequestParam("token") String token,
                                                        @Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        LOGGER.info("---- Reset password user with login: {}", resetPasswordDto.getLogin());
        userService.resetPasswordNotLoggedUser(token, resetPasswordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Report user by id")
    @PostMapping(value = "/users/reports")
    public ResponseEntity<?> reportUser(@Valid @RequestBody RequestReportDto requestReportDto) {
        LOGGER.info("---- User reports user with id: {}, for: {}",
                requestReportDto.getSuspectId(), requestReportDto.getReportType());
        userService.reportUserBySuspectId(requestReportDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Decide about report")
    @PostMapping(value = "/users/reports/{reportId}")
    public ResponseEntity<?> decideAboutReport(@PathVariable(value = "reportId") Long reportId,
                                               @RequestParam(value = "confirmation") boolean confirmation) {
        LOGGER.info("---- Admin decides about user report id: {}, confirmation: {}", reportId, confirmation);
        userService.decideAboutReport(reportId, confirmation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all reports")
    @GetMapping(value = "/users/reports")
    public ResponseEntity<List<ReportDto>> getReports() {
        LOGGER.info("---- Admin gets all user reports");
        return new ResponseEntity<>(userService.getAllUserReports(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get all users information")
    @GetMapping(value = "/users")
    public ResponseEntity<List<UserDto>> getUsers() {
        LOGGER.info("---- Get all users information");
        return new ResponseEntity<>(userService.getAllUses(), HttpStatus.OK);
    }

    @ApiOperation(value = "Change user activity status")
    @PutMapping(value = "/users")
    public ResponseEntity<?> changeActivityStatus(@RequestParam(value = "activityStatus") String status) {
        LOGGER.info("---- User changes status on: {}", status);
        userService.changeActivityStatus(status);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
