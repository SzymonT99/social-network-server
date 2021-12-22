package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.*;

import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.domain.dto.response.ReportDto;
import com.server.springboot.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
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
    @GetMapping(value = "/auth/account-activation")
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token") String token) {
        LOGGER.info("---- Activate account by token: {}", token);
        userService.activateAccount(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Resend activation account link")
    @PostMapping(value = "/auth/resend-activation")
    public ResponseEntity<?> resendActivationAccountLink(@RequestParam("userEmail") String userEmail) {
        LOGGER.info("---- Resend account user email: {}", userEmail);
        userService.resendActivationLink(userEmail);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Authenticate user")
    @PostMapping(value = "/auth/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserLoginDto userLoginDto) {
        LOGGER.info("---- Authenticate user: {}", userLoginDto.getLogin());
        JwtResponse jwtResponse = userService.loginUser(userLoginDto);

        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Refresh user token")
    @PostMapping(value = "/auth/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LOGGER.info("---- Refresh token");
        String refreshTokenStr = refreshTokenRequest.getRefreshToken();
        return new ResponseEntity<>(userService.refreshExpiredToken(refreshTokenStr), HttpStatus.OK);
    }

    @ApiOperation(value = "Log out user")
    @PostMapping(value = "/auth/logout")
    public ResponseEntity<?> logoutUser() {
        LOGGER.info("---- Logout user:");
        userService.logoutUser();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete user by id")
    @PostMapping(value = "/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "userId") Long userId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Change username")
    @PutMapping(value = "/users/username")
    public ResponseEntity<JwtResponse> changeUsername(@Valid @RequestBody ChangeUserLoginDto changeUserLoginDto) {
        return new ResponseEntity<>(new JwtResponse(), HttpStatus.OK);
    }

    @ApiOperation(value = "Change user password")
    @PutMapping(value = "/users/password")
    public ResponseEntity<JwtResponse> changePassword(@Valid @RequestBody ChangeUserPasswordDto changeUserPasswordDto) {
        return new ResponseEntity<>(new JwtResponse(), HttpStatus.OK);
    }

    @ApiOperation(value = "Change user phone number")
    @PutMapping(value = "/users/phoneNumber")
    public ResponseEntity<?> changePhoneNumber(@Valid @RequestBody ChangePhoneNumberDto changePhoneNumberDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Report user by id")
    @PostMapping(value = "/users/reports")
    public ResponseEntity<?> reportUser(@Valid @RequestBody RequestReportDto requestReportDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Decide about report")
    @PostMapping(value = "/users/reports/{reportId}")
    public ResponseEntity<?> decideAboutReport(@PathVariable(value = "reportId") Long reportId,
                                               @RequestParam(value = "confirmation") boolean confirmation) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all reports")
    @GetMapping(value = "/users/reports")
    public ResponseEntity<List<ReportDto>> getReports() {
        List<ReportDto> reportDtoList = new ArrayList<>();
        return new ResponseEntity<>(reportDtoList, HttpStatus.OK);
    }
}
