package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.CreateUserDto;
import com.server.springboot.domain.dto.request.UserLoginDto;
import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.domain.dto.response.RefreshTokenResponse;
import com.server.springboot.domain.entity.AccountVerification;
import com.server.springboot.domain.entity.RefreshToken;
import com.server.springboot.domain.entity.Role;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.domain.repository.AccountVerificationRepository;
import com.server.springboot.domain.repository.RoleRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.*;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.EmailService;
import com.server.springboot.service.RefreshTokenService;
import com.server.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final int VERIFICATION_TOKEN_EXPIRATION_TIME = 7200000;
    private final Integer MAX_LOGIN_ATTEMPTS = 5;

    @Value("${jwtAccessExpirationMs}")
    private Long accessTokenExpirationMs;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountVerificationRepository accountVerificationRepository;
    private final Converter<User, CreateUserDto> userMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           AccountVerificationRepository accountVerificationRepository,
                           Converter<User, CreateUserDto> userMapper, EmailService emailService, TemplateEngine templateEngine,
                           AuthenticationManager authenticationManager, PasswordEncoder encoder, JwtUtils jwtUtils,
                           RefreshTokenService refreshTokenService, UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountVerificationRepository = accountVerificationRepository;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void addUser(CreateUserDto createUserDto) {
        if (userRepository.existsByUsername(createUserDto.getUsername())) {
            LOGGER.info("---- Username already exist");
            throw new ForbiddenException("There is already a user with the given username");
        }
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            LOGGER.info("---- Email already exist");
            throw new ForbiddenException("There is already a user with the given email");
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        User newUser = userMapper.convert(createUserDto);
        newUser.setPassword(bCryptPasswordEncoder.encode(createUserDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(AppRole.ROLE_USER)
                .orElseThrow(() -> new NotFoundException("Not found role: ROLE_USER"));
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
        User authorizedUser = userRepository.findByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())
                .orElse(new User());
        if (userRepository.existsByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())) {
            authorizedUser.setBlocked(authorizedUser.getIncorrectLoginCounter() >= MAX_LOGIN_ATTEMPTS);
            authorizedUser.setIncorrectLoginCounter(authorizedUser.getIncorrectLoginCounter() + 1);
            userRepository.save(authorizedUser);
        }

        if (authorizedUser.getIncorrectLoginCounter() >= MAX_LOGIN_ATTEMPTS + 1 && authorizedUser.isBlocked()) {
            throw new ForbiddenException(String.format("User account with login: %s is blocked", userLoginDto.getLogin()));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.getLogin(), userLoginDto.getPassword()));

        // Gdy autentykacja jest pomyślna, wykonywany jest poniższy kod
        authorizedUser.setIncorrectLoginCounter(0);
        authorizedUser.setActivityStatus(ActivityStatus.ONLINE);
        userRepository.save(authorizedUser);

        if (!authorizedUser.isVerifiedAccount()) {
            throw new ForbiddenException(String.format("User account with login: %s has not been activated", userLoginDto.getLogin()));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        return new JwtResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                accessToken,
                "Bearer",
                accessTokenExpirationMs,
                refreshToken.getToken());
    }

    @Override
    public RefreshTokenResponse refreshExpiredToken(String refreshTokenStr) {
        RefreshToken lastRefreshToken = refreshTokenService.findToken(refreshTokenStr)
                .orElseThrow(() -> new NotFoundException("Not found received refreshToken: " + refreshTokenStr));
        if (refreshTokenService.checkExpirationDate(lastRefreshToken)) {
            User user = lastRefreshToken.getUser();
            String username = user.getUsername();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtUtils.generateAccessToken(userDetails);
            String newRefreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();

            return new RefreshTokenResponse(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer"
            );
        }
        return new RefreshTokenResponse();
    }

    @Override
    public void logoutUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + userId));
        refreshTokenService.deleteByUser(user);
    }

}
