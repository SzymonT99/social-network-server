package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.*;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final int VERIFICATION_TOKEN_EXPIRATION_TIME = 7200000;
    private static final int RESET_PASSWORD_TOKEN_EXPIRATION_TIME = 3600000;
    private final Integer MAX_LOGIN_ATTEMPTS = 5;

    private static final Long accessTokenExpirationMs = 14400000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ReportRepository reportRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final AccountVerificationRepository accountVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserMapper userMapper;
    private final ReportMapper reportMapper;
    private final UserDtoListMapper userDtoListMapper;
    private final ReportDtoListMapper reportDtoListMapper;
    private final UserAccountDtoListMapper userAccountDtoListMapper;
    private final NotificationService notificationService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository,
                           ReportRepository reportRepository, PasswordResetRepository passwordResetRepository,
                           AccountVerificationRepository accountVerificationRepository,
                           RefreshTokenRepository refreshTokenRepository, UserMapper userMapper,
                           EmailService emailService, TemplateEngine templateEngine,
                           AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                           RefreshTokenService refreshTokenService, UserDetailsServiceImpl userDetailsService,
                           ReportMapper reportMapper,
                           ReportDtoListMapper reportDtoListMapper,
                           UserDtoListMapper userDtoListMapper,
                           NotificationService notificationService,
                           UserAccountDtoListMapper userAccountDtoListMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.reportRepository = reportRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.accountVerificationRepository = accountVerificationRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.reportMapper = reportMapper;
        this.reportDtoListMapper = reportDtoListMapper;
        this.userDtoListMapper = userDtoListMapper;
        this.notificationService = notificationService;
        this.userAccountDtoListMapper = userAccountDtoListMapper;
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

        User newUser = userMapper.convert(createUserDto);
        newUser.setPassword(passwordEncoder.encode(createUserDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(AppRole.ROLE_USER)
                .orElseThrow(() -> new NotFoundException("Not found role: ROLE_USER"));
        roles.add(userRole);
        newUser.setRoles(roles);

        userRepository.save(newUser);

        String activationCode = UUID.randomUUID().toString();
        AccountVerification accountVerification = new AccountVerification(newUser, activationCode, VERIFICATION_TOKEN_EXPIRATION_TIME);
        accountVerificationRepository.save(accountVerification);

        String activationLink = "http://localhost:3000/auth/activate-account/" + activationCode;
        Context context = new Context();
        context.setVariable("link", activationLink);
        context.setVariable("name", createUserDto.getFirstName() + " " + createUserDto.getLastName());
        String html = templateEngine.process("EmailActivationTemplate", context);
        emailService.sendEmail(createUserDto.getEmail(), "Serwis społecznościowy - aktywacja konta", html);
    }

    @Override
    public ActivatedAccountDto activateAccount(String token) {
        AccountVerification accountVerification = accountVerificationRepository.findByVerificationCode(token)
                .orElseThrow(() -> new NotFoundException("The specified account activation code was not found"));

        User user = accountVerification.getUser();
        boolean verifiedAccount = user.isVerifiedAccount();
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

        return ActivatedAccountDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .userEmail(user.getEmail())
                .build();
    }

    @Override
    public JwtResponse loginUser(UserLoginDto userLoginDto) {
        User authorizedUser = userRepository.findByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())
                .orElse(new User());

        if (userRepository.existsByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())) {
            authorizedUser.setBlocked(authorizedUser.getIncorrectLoginCounter() >= MAX_LOGIN_ATTEMPTS);
            authorizedUser.setIncorrectLoginCounter(authorizedUser.getIncorrectLoginCounter() + 1);
            userRepository.save(authorizedUser);

            if (authorizedUser.getIncorrectLoginCounter() >= MAX_LOGIN_ATTEMPTS + 1 && authorizedUser.isBlocked()) {
                throw new ForbiddenException(String.format("User account with login: %s is blocked", userLoginDto.getLogin()));
            }
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.getLogin(), userLoginDto.getPassword()));

        // Gdy autentykacja jest pomyślna, wykonywany jest poniższy kod
        authorizedUser.setIncorrectLoginCounter(0);
        authorizedUser.setActivityStatus(ActivityStatus.ONLINE);
        userRepository.save(authorizedUser);

        if (authorizedUser.isBanned()) {
            throw new ForbiddenException(String.format("User account with login: %s has been banned. " +
                    "In order to unblock the account, please contact the admin", userLoginDto.getLogin()));
        }

        if (!authorizedUser.isVerifiedAccount()) {
            throw new ConflictRequestException(String.format("User account with login: %s has not been activated", userLoginDto.getLogin()));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        LocalDateTime accessTokenExpirationDate = LocalDateTime.now().plus(accessTokenExpirationMs, ChronoField.MILLI_OF_DAY.getBaseUnit());

        informUserFriendsAboutActivity(authorizedUser);

        return new JwtResponse(
                userDetails.getUserId(),
                roles,
                accessToken,
                "Bearer",
                accessTokenExpirationDate.toString(),
                refreshToken.getToken());
    }

    @Override
    public RefreshTokenResponse refreshExpiredToken(String refreshTokenStr) {
        RefreshToken lastRefreshToken = refreshTokenService.findToken(refreshTokenStr)
                .orElseThrow(() -> new NotFoundException("Not found received refreshToken: " + refreshTokenStr));
        if (refreshTokenService.checkExpirationDate(lastRefreshToken)) {
            User user = lastRefreshToken.getUser();
            String username = user.getUsername();
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtUtils.generateAccessToken(userDetails);
            String newRefreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();

            refreshTokenRepository.delete(lastRefreshToken);

            LocalDateTime newAccessTokenExpirationDate = LocalDateTime.now().plus(accessTokenExpirationMs, ChronoField.MILLI_OF_DAY.getBaseUnit());

            return new RefreshTokenResponse(
                    newAccessToken,
                    newAccessTokenExpirationDate.toString(),
                    newRefreshToken,
                    "Bearer"
            );
        }
        return new RefreshTokenResponse();
    }

    @Override
    public void logoutUser(Long userId) {
        LOGGER.info("---- Logout user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + userId));
        user.setActivityStatus(ActivityStatus.OFFLINE);
        refreshTokenService.deleteByUser(user);

        informUserFriendsAboutActivity(user);
    }

    @Override
    public void resendActivationLink(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Not found user with given email: " + userEmail));
        String activationCode = UUID.randomUUID().toString();
        AccountVerification accountVerification = new AccountVerification(user, activationCode, VERIFICATION_TOKEN_EXPIRATION_TIME);
        accountVerificationRepository.save(accountVerification);

        String activationLink = "http://localhost:3000/auth/activate-account/" + activationCode;
        Context context = new Context();
        context.setVariable("link", activationLink);
        context.setVariable("name", user.getUserProfile().getFirstName() + " " + user.getUserProfile().getLastName());
        String html = templateEngine.process("EmailActivationTemplate", context);
        emailService.sendEmail(userEmail, "Serwis społecznościowy - aktywacja konta", html);
    }

    @Override
    public void deleteUser(DeleteUserDto deleteUserDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(deleteUserDto.getLogin(), deleteUserDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByUsernameOrEmail(deleteUserDto.getLogin(), deleteUserDto.getLogin())
                .orElseThrow(() -> new NotFoundException("Not found user with given login: " + deleteUserDto.getLogin()));

        userRepository.delete(user);
    }

    @Override
    public JwtResponse changeUsername(Long userId, ChangeUsernameDto changeUsernameDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + userId));
        if (!user.getUsername().equals(changeUsernameDto.getOldUsername())) {
            throw new ForbiddenException("The current username is not correct");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(changeUsernameDto.getOldUsername(), changeUsernameDto.getPassword()));

        if (userRepository.existsByUsername(changeUsernameDto.getNewUsername())) {
            LOGGER.info("---- Username already exist");
            throw new ForbiddenException("There is already a user with the given username");
        }

        user.setUsername(changeUsernameDto.getNewUsername());
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userDetails.setUsername(changeUsernameDto.getNewUsername());
        String accessToken = jwtUtils.generateAccessToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        LocalDateTime accessTokenExpirationDate = LocalDateTime.now().plus(accessTokenExpirationMs, ChronoField.MILLI_OF_DAY.getBaseUnit());

        return new JwtResponse(
                userDetails.getUserId(),
                roles,
                accessToken,
                "Bearer",
                accessTokenExpirationDate.toString(),
                refreshToken.getToken());
    }

    @Override
    public JwtResponse changeEmail(Long userId, ChangeEmailDto changeEmailDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + userId));
        if (!user.getEmail().equals(changeEmailDto.getOldEmail())) {
            throw new ForbiddenException("The current email provided is not correct");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(changeEmailDto.getOldEmail(), changeEmailDto.getPassword()));

        if (userRepository.existsByEmail(changeEmailDto.getNewEmail())) {
            LOGGER.info("---- Email already exist");
            throw new ForbiddenException("There is already a user with the given email");
        }

        user.setEmail(changeEmailDto.getNewEmail());
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userDetails.setEmail(changeEmailDto.getNewEmail());
        String accessToken = jwtUtils.generateAccessToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        LocalDateTime accessTokenExpirationDate = LocalDateTime.now().plus(accessTokenExpirationMs, ChronoField.MILLI_OF_DAY.getBaseUnit());

        return new JwtResponse(
                userDetails.getUserId(),
                roles,
                accessToken,
                "Bearer",
                accessTokenExpirationDate.toString(),
                refreshToken.getToken());
    }

    @Override
    public JwtResponse changePassword(Long userId, ChangeUserPasswordDto changeUserPasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + userId));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), changeUserPasswordDto.getOldPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (!changeUserPasswordDto.getNewPassword().equals(changeUserPasswordDto.getRepeatedNewPassword())) {
            throw new BadRequestException("Re-entered new password is not correct");
        }

        user.setPassword(passwordEncoder.encode(changeUserPasswordDto.getNewPassword()));
        userRepository.save(user);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        LocalDateTime accessTokenExpirationDate = LocalDateTime.now().plus(accessTokenExpirationMs, ChronoField.MILLI_OF_DAY.getBaseUnit());

        return new JwtResponse(
                userDetails.getUserId(),
                roles,
                accessToken,
                "Bearer",
                accessTokenExpirationDate.toString(),
                refreshToken.getToken());
    }

    @Override
    public void changePhoneNumber(Long userId, ChangePhoneNumberDto changePhoneNumberDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + userId));
        if (!user.getPhoneNumber().equals(changePhoneNumberDto.getOldPhoneNumber())) {
            throw new ForbiddenException("The current phone number provided is not correct");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), changePhoneNumberDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        user.setPhoneNumber(changePhoneNumberDto.getNewPhoneNumber());
        userRepository.save(user);
    }

    @Override
    public void reportUserBySuspectId(RequestReportDto requestReportDto) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        User suspectUser = userRepository.findById(requestReportDto.getSuspectId())
                .orElseThrow(() -> new NotFoundException("Not found user with given id: " + requestReportDto.getSuspectId()));
        Report report = reportMapper.convert(requestReportDto);
        report.setSuspect(suspectUser);
        report.setSender(loggedUser);
        reportRepository.save(report);
    }

    @Override
    public void decideAboutReport(Long reportId, boolean confirmation) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Not found report with given id: " + reportId));
        report.setIsConfirmed(confirmation);
        User punishedUser = report.getSuspect();
        punishedUser.setBanned(true);

        reportRepository.save(report);
        userRepository.save(punishedUser);
    }

    @Override
    public List<ReportDto> getAllUserReports() {
        List<Report> reports = reportRepository.findByOrderByCreatedAtDesc();
        return reportDtoListMapper.convert(reports);
    }

    @Override
    public void sendResetPasswordToken(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Not found user with given email: " + userEmail));
        String resetCode = UUID.randomUUID().toString();
        PasswordReset passwordReset = new PasswordReset(resetCode, user, RESET_PASSWORD_TOKEN_EXPIRATION_TIME);
        passwordResetRepository.save(passwordReset);

        String activationLink = "http://localhost:3000/auth/reset-password/" + resetCode;
        Context context = new Context();
        context.setVariable("link", activationLink);
        context.setVariable("name", user.getUserProfile().getFirstName() + " " + user.getUserProfile().getLastName());
        String html = templateEngine.process("EmailResetPasswordTemplate", context);
        emailService.sendEmail(userEmail, "Serwis społecznościowy - resetowanie hasła", html);
    }

    @Override
    public void resetPasswordNotLoggedUser(String token, ResetPasswordDto resetPasswordDto) {
        User user = userRepository.findByUsernameOrEmail(resetPasswordDto.getLogin(), resetPasswordDto.getLogin())
                .orElseThrow(() -> new NotFoundException("Not found user with given login: " + resetPasswordDto.getLogin()));
        if (!resetPasswordDto.getNewPassword().equals(resetPasswordDto.getRepeatedNewPassword())) {
            throw new BadRequestException("Re-entered new password is not correct");
        }

        PasswordReset passwordReset = passwordResetRepository.findByUserAndResetCode(user, token)
                .orElseThrow(() -> new NotFoundException("Not found reset code for user id: " + user.getUserId()));
        if (passwordReset.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResourceGoneException("The reset password link has expired on " + passwordReset.getExpiredAt());
        } else {
            if (passwordReset.isUsed()) {
                throw new ConflictRequestException("The reset link has already been used");
            } else {
                passwordReset.setUsed(true);
                passwordResetRepository.save(passwordReset);
                user.setPassword(resetPasswordDto.getNewPassword());
                userRepository.save(user);
            }
        }
    }

    @Override
    public List<UserDto> getAllUses() {
        List<User> users = userRepository.findAll();
        System.out.println(userDtoListMapper);
        return userDtoListMapper.convert(users);
    }

    @Override
    public void changeActivityStatus(String status) {
        Long loggedUserId = jwtUtils.getLoggedUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        loggedUser.setActivityStatus(ActivityStatus.valueOf(status));
        userRepository.save(loggedUser);

        informUserFriendsAboutActivity(loggedUser);
    }

    @Override
    public UserAccountPageDto getUsersAccounts(Integer page, Integer size) {
        Role adminRole = roleRepository.findByName(AppRole.ROLE_ADMIN)
                .orElseThrow(() -> new NotFoundException("Not found role: " + AppRole.ROLE_ADMIN));

        Pageable paging = PageRequest.of(page, size);

        Page<User> pageUsers = userRepository.findByRolesNotContaining(adminRole, paging);
        List<User> users = pageUsers.getContent();

        List<UserAccountDto> accountDtoList = userAccountDtoListMapper.convert(users);

        return UserAccountPageDto.builder()
                .userAccounts(accountDtoList)
                .currentPage(pageUsers.getNumber())
                .totalItems(pageUsers.getTotalElements())
                .totalPages(pageUsers.getTotalPages())
                .build();
    }

    @Override
    public void manageUserAccount(Long userId, UserAccountUpdateDto userAccountUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        UserProfile userProfile = user.getUserProfile();


        user.setUsername(userAccountUpdateDto.getUsername());
        user.setEmail(userAccountUpdateDto.getEmail());
        user.setPhoneNumber(userAccountUpdateDto.getPhoneNumber());
        user.setIncorrectLoginCounter(userAccountUpdateDto.getIncorrectLoginCounter());
        user.setVerifiedAccount(userAccountUpdateDto.isActivateAccount());
        user.setBlocked(userAccountUpdateDto.isBlocked());
        user.setBanned(userAccountUpdateDto.isBanned());

        userProfile.setFirstName(userAccountUpdateDto.getFirstName());
        userProfile.setLastName(userAccountUpdateDto.getLastName());
        userProfile.setPublic(userAccountUpdateDto.isPublicProfile());

        user.setUserProfile(userProfile);

        userRepository.save(user);
    }

    @Override
    public void deleteUserByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        userRepository.delete(user);
    }

    public void informUserFriendsAboutActivity(User user) {
        if (user.getUserFriends() != null) {
            List<User> userFriendList = user.getUserFriends().stream()
                    .map(Friend::getUser)
                    .collect(Collectors.toList());

            for (User userFriend : userFriendList) {
                notificationService.sendNotificationToUser(user, userFriend.getUserId(), ActionType.FRIENDS_STATUS);
            }
        }

    }
}
