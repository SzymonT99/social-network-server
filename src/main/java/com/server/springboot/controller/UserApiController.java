package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.CreateUserDto;

import com.server.springboot.domain.dto.request.RefreshTokenRequest;
import com.server.springboot.domain.dto.request.UserLoginDto;
import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.service.UserService;
import com.server.springboot.service.impl.RefreshTokenServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @PostMapping("/auth/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        LOGGER.info("---- Create user: {}", createUserDto.getUsername());
        userService.addUser(createUserDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/account-activation")
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token") String token) {
        LOGGER.info("---- Activate account by token: {}", token);
        userService.activateAccount(token);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserLoginDto userLoginDto) {
        LOGGER.info("---- Authenticate user: {}", userLoginDto.getLogin());
        JwtResponse jwtResponse = userService.loginUser(userLoginDto);

        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }

    @PostMapping("/auth/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LOGGER.info("---- Refresh token");
        String refreshTokenStr = refreshTokenRequest.getRefreshToken();
        return new ResponseEntity<>(userService.refreshExpiredToken(refreshTokenStr), HttpStatus.OK);
    }

    @PostMapping("/auth/logout/{user_id}")
    public ResponseEntity<?> logoutUser(@PathVariable("user_id") Long userId) {
        LOGGER.info("---- Logout user with id: {}", userId);
        userService.logoutUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user")
    public ResponseEntity<?> user() {

        return new ResponseEntity<>("USER", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> admin() {

        return new ResponseEntity<>("ADMIN", HttpStatus.OK);
    }

}
