package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.CreateUserDto;

import com.server.springboot.domain.repository.AccountVerificationRepository;
import com.server.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);

    private final UserService userService;
    private final AccountVerificationRepository accountVerificationRepository;

    @Autowired
    public UserApiController(UserService userService, AccountVerificationRepository accountVerificationRepository) {
        this.userService = userService;
        this.accountVerificationRepository = accountVerificationRepository;
    }

    @PostMapping("/register")
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

}
