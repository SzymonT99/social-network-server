package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.CreateUserDto;
import com.server.springboot.domain.dto.request.UserLoginDto;
import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.domain.entity.AccountVerification;
import com.server.springboot.domain.entity.Role;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.mapper.UserMapper;
import com.server.springboot.domain.repository.AccountVerificationRepository;
import com.server.springboot.domain.repository.RoleRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ExistingDataException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.exception.ResourceGoneException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.EmailService;
import com.server.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final int VERIFICATION_TOKEN_EXPIRATION_TIME = 7200000;

    @Value("${jwtExpirationMs}")
    private int jwtExpirationMs;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountVerificationRepository accountVerificationRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           AccountVerificationRepository accountVerificationRepository,
                           UserMapper userMapper, EmailService emailService, TemplateEngine templateEngine,
                           AuthenticationManager authenticationManager, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountVerificationRepository = accountVerificationRepository;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
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

        List<Role> roles = new ArrayList<>();
        Role userRole = roleRepository.findByName(AppRole.ROLE_USER)
                .orElseThrow(() -> new NotFoundException("Not found role"));
        roles.add(userRole);
        newUser.setRoles(roles);

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

    @Override
    public JwtResponse loginUser(UserLoginDto userLoginDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles, jwt,
                "Bearer",
                jwtExpirationMs);
    }

}
