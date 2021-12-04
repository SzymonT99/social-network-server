package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.CreateUserDto;
import com.server.springboot.domain.entity.AccountVerification;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.mapper.UserMapper;
import com.server.springboot.domain.repository.AccountVerificationRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ExistingDataException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.exception.ResourceGoneException;
import com.server.springboot.service.EmailService;
import com.server.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final int VERIFICATION_TOKEN_EXPIRATION_TIME = 7200000;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final AccountVerificationRepository accountVerificationRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AccountVerificationRepository accountVerificationRepository, UserMapper userMapper, EmailService emailService, TemplateEngine templateEngine) {
        this.userRepository = userRepository;
        this.accountVerificationRepository = accountVerificationRepository;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    @Override
    public void addUser(CreateUserDto createUserDto) {
        if (userRepository.existsByUsername(createUserDto.getUsername())) {
            LOGGER.info("---- username already exist");
            throw new ExistingDataException("username");
        }
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            LOGGER.info("---- email already exist");
            throw new ExistingDataException("email");
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        User newUser = userMapper.convert(createUserDto);
        newUser.setPassword(bCryptPasswordEncoder.encode(createUserDto.getPassword()));
        userRepository.save(newUser);

        String activationCode = UUID.randomUUID().toString();
        AccountVerification accountVerification = new AccountVerification(newUser, activationCode, VERIFICATION_TOKEN_EXPIRATION_TIME);
        accountVerificationRepository.save(accountVerification);

        String activationLink = "CLIENT_URL?token=" + activationCode;
        Context context = new Context();
        context.setVariable("link", activationLink);
        context.setVariable("name", createUserDto.getFirstName() + " " + createUserDto.getLastName());
        String html = templateEngine.process("ActivationAccount", context);
        emailService.sendEmail(createUserDto.getEmail(), "Serwis społecznościowy - aktywacja konta", html);
    }

    @Override
    public void activateAccount(String token) {
        AccountVerification accountVerification = accountVerificationRepository.findByVerificationCode(token)
                .orElseThrow(() -> new NotFoundException("The specified account activation code was not found"));

        User user = accountVerification.getUser();
        boolean verifiedAccount = userRepository.findByUsername(user.getUsername()).get().isVerifiedAccount();
        if (verifiedAccount) {
            LOGGER.info("---- Account is already activated");
            throw new BadRequestException("The account has already been activated");
        }
        if (accountVerification.getExpiredAt().isBefore(LocalDateTime.now())) {
            LOGGER.info("---- Activation link has expired");
            throw new ResourceGoneException("The account activation link has expired on " + accountVerification.getExpiredAt());
        }

        user.setVerifiedAccount(true);
        userRepository.save(user);
    }

}
