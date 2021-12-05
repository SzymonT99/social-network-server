package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.CreateUserDto;

import com.server.springboot.domain.dto.request.UserLoginDto;
import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.domain.repository.RoleRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public UserApiController(UserService userService, AuthenticationManager authenticationManager,
                             UserRepository userRepository, RoleRepository roleRepository,
                             PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        LOGGER.info("---- Create user");
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
        LOGGER.info("---- Authenticate user");
        JwtResponse jwtResponse = userService.loginUser(userLoginDto);

        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }

}
