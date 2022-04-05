package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.enumeration.ReportType;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.ResourceGoneException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.EmailService;
import com.server.springboot.service.NotificationService;
import com.server.springboot.service.RefreshTokenService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private PasswordResetRepository passwordResetRepository;
    @Mock
    private AccountVerificationRepository accountVerificationRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    @Mock
    private NotificationService notificationService;
    @Spy
    private UserMapper userMapper;
    @Spy
    private ReportMapper reportMapper;
    @Spy
    private UserDtoListMapper userDtoListMapper;
    @Spy
    private ReportDtoListMapper reportDtoListMapper;
    @Spy
    private UserAccountDtoListMapper userAccountDtoListMapper;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {

        userMapper = new UserMapper();
        reportMapper = new ReportMapper();
        userDtoListMapper = new UserDtoListMapper();
        userAccountDtoListMapper = new UserAccountDtoListMapper();
        reportDtoListMapper = new ReportDtoListMapper();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        user = User.builder()
                .userId(1L)
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .verifiedAccount(false)
                .activityStatus(ActivityStatus.OFFLINE)
                .isBlocked(false)
                .isBanned(false)
                .userProfile(UserProfile.builder()
                        .firstName("Jan")
                        .lastName("Nowak")
                        .gender(Gender.MALE)
                        .dateOfBirth(LocalDate.parse("1989-01-05", formatter))
                        .age(LocalDate.now().getYear() - LocalDate.parse("1989-01-05", formatter).getYear())
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();
    }

    @Test
    public void shouldAddUserSuccessfully() {
        CreateUserDto createUser = CreateUserDto.builder()
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .firstName("Jan")
                .lastName("Nowak")
                .gender(Gender.MALE)
                .dateOfBirth("1989-01-05")
                .build();

        when(userRepository.existsByEmail(createUser.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(createUser.getUsername())).thenReturn(false);
        when(roleRepository.findByName(AppRole.ROLE_USER)).thenReturn(Optional.of(new Role(1, AppRole.ROLE_USER)));

        assertDoesNotThrow(() -> userService.addUser(createUser));
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmail(eq(createUser.getEmail()), any(), any());
    }

    @Test
    public void shouldThrowErrorWhenCreateUserWithExistingEmail() {
        CreateUserDto createUser = CreateUserDto.builder()
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .firstName("Jan")
                .lastName("Nowak")
                .gender(Gender.MALE)
                .dateOfBirth("1989-01-05")
                .build();

        when(userRepository.existsByEmail(createUser.getEmail())).thenReturn(true);
        when(userRepository.existsByUsername(createUser.getUsername())).thenReturn(false);

        Exception exception = assertThrows(ForbiddenException.class, () ->
                userService.addUser(createUser)
        );

        String expectedMessage = "There is already a user with the given email";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldActivateAccount() {
        String activationToken = "17f2b98a-8874-4b2d-bc0a-0ca1aec13aa4";

        AccountVerification savedAccountVerification = AccountVerification.builder()
                .verificationId(1L)
                .verificationCode(activationToken)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(1L))
                .user(user)
                .build();

        when(accountVerificationRepository.findByVerificationCode(activationToken))
                .thenReturn(Optional.of(savedAccountVerification));

        ActivatedAccountDto resultActivatedAccountDto = userService.activateAccount(activationToken);

        assertNotNull(resultActivatedAccountDto);
        assertEquals(user.getEmail(), resultActivatedAccountDto.getUserEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldThrowErrorWhenActivateAccountWithExpiredToken() {
        String activationToken = "17f2b98a-8874-4b2d-bc0a-0ca1aec13aa4";

        AccountVerification savedAccountVerification = AccountVerification.builder()
                .verificationId(1L)
                .verificationCode(activationToken)
                .createdAt(LocalDateTime.now().minusDays(2L))
                .expiredAt(LocalDateTime.now().minusDays(1L))
                .user(user)
                .build();

        when(accountVerificationRepository.findByVerificationCode(activationToken))
                .thenReturn(Optional.of(savedAccountVerification));

        Exception exception = assertThrows(ResourceGoneException.class, () ->
                userService.activateAccount(activationToken)
        );

        String expectedMessage = "The account activation link has expired on " + savedAccountVerification.getExpiredAt();
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldAuthenticateUserSuccessfully() {
        String expectedRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikphbj" +
                "EyMyIsImlhdCI6MTUxNjIzOTAyMn0.RHe9olPqW2BiehhUJz6QZ1lpUezlQXlpE6TwlezKHL0";
        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("Jan123")
                .password("Qwertyuiop")
                .build();
        user.setVerifiedAccount(true);

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.getLogin(), userLoginDto.getPassword()))
        ).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(UserDetailsImpl.build(user));

        when(userRepository.findByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())).thenReturn(true);

        when(refreshTokenService.createRefreshToken(userLoginDto.getLogin()))
                .thenReturn(RefreshToken.builder().token(expectedRefreshToken).build());

        JwtResponse jwtResponse = userService.loginUser(userLoginDto);

        assertNotNull(jwtResponse);
        assertEquals(user.getUserId(), jwtResponse.getUserId());
        assertTrue(LocalDateTime.parse(jwtResponse.getAccessTokenExpirationDate()).isAfter(LocalDateTime.now()));
    }

    @Test
    public void shouldThrowErrorWhenAuthenticatedUserIsNotVerified() {
        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("Jan123")
                .password("Qwertyuiop")
                .build();

        when(userRepository.findByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())).thenReturn(true);

        Exception exception = assertThrows(ConflictRequestException.class, () ->
                userService.loginUser(userLoginDto)
        );

        String expectedMessage = String.format("User account with login: %s has not been activated", userLoginDto.getLogin());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void shouldThrowErrorWhenNumberOfLoginAttemptIsGreaterThan5() {
        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("Jan123")
                .password("Qwertyuiop")
                .build();
        user.setVerifiedAccount(true);
        user.setIncorrectLoginCounter(6);

        when(userRepository.findByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())).thenReturn(true);

        Exception exception = assertThrows(ForbiddenException.class, () ->
                userService.loginUser(userLoginDto)
        );

        String expectedMessage = String.format("User account with login: %s is blocked", userLoginDto.getLogin());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
        verify(authenticationManager, never()).authenticate(any(Authentication.class));
    }

    @Test
    public void shouldThrowErrorWhenAuthenticatedUserIsBanned() {
        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("Jan123")
                .password("Qwertyuiop")
                .build();
        user.setVerifiedAccount(true);
        user.setBanned(true);

        when(userRepository.findByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())).thenReturn(true);

        Exception exception = assertThrows(ForbiddenException.class, () ->
                userService.loginUser(userLoginDto)
        );

        String expectedMessage = String.format("User account with login: %s has been banned. " +
                "In order to unblock the account, please contact the admin", userLoginDto.getLogin());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void shouldRejectUserAuthentication() {
        UserLoginDto userLoginDto = UserLoginDto.builder()
                .login("Jan123")
                .password("wrongPassword")
                .build();
        user.setVerifiedAccount(true);

        when(userRepository.findByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameOrEmail(userLoginDto.getLogin(), userLoginDto.getLogin())).thenReturn(true);
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.getLogin(), userLoginDto.getPassword()))
        ).thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> {
            userService.loginUser(userLoginDto);
        });
    }

    @Test
    public void shouldRefreshUserToken() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikphbj" +
                "EyMyIsImlhdCI6MTUxNjIzOTAyMn0.RHe9olPqW2BiehhUJz6QZ1lpUezlQXlpE6TwlezKHL0";
        RefreshToken refreshToken = RefreshToken.builder()
                .refreshTokenId(1L)
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusWeeks(1L))
                .user(user)
                .build();

        when(refreshTokenService.findToken(token)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.checkExpirationDate(refreshToken)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(UserDetailsImpl.build(user));

        refreshToken.setToken(UUID.randomUUID().toString());
        when(refreshTokenService.createRefreshToken(user.getUsername())).thenReturn(refreshToken);

        RefreshTokenResponse refreshTokenResponse = userService.refreshExpiredToken(token);

        assertNotNull(refreshTokenResponse);
    }

    @Test
    public void shouldLogoutUser() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenService).deleteByUser(user);

        userService.logoutUser(userId);

        verify(refreshTokenService, times(1)).deleteByUser(user);
    }

    @Test
    public void shouldResendActivationLink() {
        String userEmail = "janNowak@gmail.com";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(accountVerificationRepository.save(any(AccountVerification.class))).thenReturn(any(AccountVerification.class));

        userService.resendActivationLink(userEmail);

        verify(emailService, times(1))
                .sendEmail(eq(userEmail), eq("Serwis społecznościowy - aktywacja konta"), any());
    }

    @Test
    public void shouldDeleteUser() {
        DeleteUserDto deleteUserDto = DeleteUserDto.builder()
                .login("Jan123")
                .password("Qwertyuiop")
                .build();

        when(userRepository.findByUsernameOrEmail(deleteUserDto.getLogin(), deleteUserDto.getLogin()))
                .thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(deleteUserDto.getLogin(), deleteUserDto.getPassword()))
        ).thenReturn(authentication);
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(deleteUserDto);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void shouldChangeUsername() {
        Long userId = 1L;
        ChangeUsernameDto changeUsernameDto = ChangeUsernameDto.builder()
                .oldUsername("Jan123")
                .newUsername("Jan321")
                .password("Qwertyuiop")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(changeUsernameDto.getOldUsername(), changeUsernameDto.getPassword()))
        ).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(UserDetailsImpl.build(user));

        when(userRepository.existsByUsername(changeUsernameDto.getNewUsername())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(refreshTokenService.createRefreshToken(changeUsernameDto.getNewUsername()))
                .thenReturn(RefreshToken.builder().token(UUID.randomUUID().toString()).build());

        JwtResponse jwtResponse = userService.changeUsername(userId, changeUsernameDto);
        String updatedUsername = userRepository.findById(userId).get().getUsername();

        assertNotNull(jwtResponse);
        assertEquals(userId, jwtResponse.getUserId());
        assertEquals(changeUsernameDto.getNewUsername(), updatedUsername);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldChangeEmail() {
        Long userId = 1L;
        ChangeEmailDto changeEmailDto = ChangeEmailDto.builder()
                .oldEmail("janNowak@gmail.com")
                .newEmail("janNowak123@gmail.com")
                .password("Qwertyuiop")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(changeEmailDto.getOldEmail(), changeEmailDto.getPassword()))
        ).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(UserDetailsImpl.build(user));

        when(userRepository.existsByEmail(changeEmailDto.getNewEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(refreshTokenService.createRefreshToken(anyString()))
                .thenReturn(RefreshToken.builder().token(UUID.randomUUID().toString()).build());

        JwtResponse jwtResponse = userService.changeEmail(userId, changeEmailDto);
        String updatedEmail = userRepository.findById(userId).get().getEmail();

        assertNotNull(jwtResponse);
        assertEquals(userId, jwtResponse.getUserId());
        assertEquals(changeEmailDto.getNewEmail(), updatedEmail);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldChangePassword() {
        Long userId = 1L;
        ChangeUserPasswordDto changeUserPasswordDto = ChangeUserPasswordDto.builder()
                .oldPassword("Qwertyuiop")
                .newPassword("newPassword")
                .repeatedNewPassword("newPassword")
                .build();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()))
        ).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(UserDetailsImpl.build(user));

        when(userRepository.save(user)).thenReturn(user);
        when(refreshTokenService.createRefreshToken(anyString()))
                .thenReturn(RefreshToken.builder().token(UUID.randomUUID().toString()).build());

        JwtResponse jwtResponse = userService.changePassword(userId, changeUserPasswordDto);
        String updatedPassword = userRepository.findById(userId).get().getPassword();

        assertNotNull(jwtResponse);
        assertEquals(userId, jwtResponse.getUserId());
        assertTrue(bCryptPasswordEncoder.matches(changeUserPasswordDto.getNewPassword(), updatedPassword));

        verify(userRepository, times(1)).save(user);
    }


    @Test
    public void shouldChangePhoneNumber() {
        Long userId = 1L;
        ChangePhoneNumberDto changePhoneNumberDto = ChangePhoneNumberDto.builder()
                .oldPhoneNumber("123456789")
                .newPhoneNumber("987654321")
                .password("Qwertyuiop")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()))
        ).thenReturn(authentication);

        when(userRepository.save(user)).thenReturn(user);

        userService.changePhoneNumber(userId, changePhoneNumberDto);
        String updatedPhoneNumber = userRepository.findById(userId).get().getPhoneNumber();

        assertEquals(changePhoneNumberDto.getNewPhoneNumber(), updatedPhoneNumber);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldSendReportBySuspectId() {
        User suspectUser = User.builder()
                .userId(2L)
                .username("Adam123")
                .password("Qwertyuiop")
                .email("adamNowak@gmail.com")
                .phoneNumber("123456789")
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();

        RequestReportDto requestReportDto = RequestReportDto.builder()
                .suspectId(2L)
                .reportType(ReportType.RUDE_POST.toString())
                .description("Opis")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(suspectUser));

        userService.reportUserBySuspectId(requestReportDto);

        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    public void shouldAdminDecideAboutReport() {
        User suspectUser = User.builder()
                .userId(2L)
                .username("Adam123")
                .password("Qwertyuiop")
                .email("adamNowak@gmail.com")
                .phoneNumber("123456789")
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();

        Long reportId = 1L;
        Report report = Report.builder()
                .reportId(1L)
                .createdAt(LocalDateTime.now())
                .reportType(ReportType.RUDE_POST)
                .description("Opis")
                .sender(user)
                .suspect(suspectUser)
                .isConfirmed(false)
                .build();

        boolean confirmation = true;

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);
        report.setIsConfirmed(confirmation);

        userService.decideAboutReport(reportId, confirmation);

        verify(reportRepository, times(1)).save(report);
    }

    @Test
    public void shouldGetAllReports() {
        List<Report> savedReports = new ArrayList<>();
        User suspectUser = User.builder()
                .userId(2L)
                .username("Adam123")
                .password("Qwertyuiop")
                .email("adamNowak@gmail.com")
                .phoneNumber("123456789")
                .userProfile(UserProfile.builder()
                        .firstName("Adam")
                        .lastName("Nowak")
                        .gender(Gender.MALE)
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();

        Report report1 = Report.builder()
                .reportId(1L)
                .createdAt(LocalDateTime.now())
                .reportType(ReportType.RUDE_POST)
                .description("Opis 1")
                .sender(user)
                .suspect(suspectUser)
                .isConfirmed(false)
                .build();

        Report report2 = Report.builder()
                .reportId(2L)
                .createdAt(LocalDateTime.now())
                .reportType(ReportType.RUDE_COMMENT)
                .description("Opis 2")
                .sender(user)
                .suspect(suspectUser)
                .isConfirmed(false)
                .build();

        savedReports.add(report1);
        savedReports.add(report2);

        when(reportRepository.findByOrderByCreatedAtDesc()).thenReturn(savedReports);

        List<ReportDto> resultReports = userService.getAllUserReports();
        assertNotNull(resultReports);
        assertEquals(2, resultReports.size());
    }

    @Test
    public void shouldSendResetPasswordToken() {
        String userEmail = "janNowak@gmail.com";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(passwordResetRepository.save(any(PasswordReset.class))).thenReturn(any(PasswordReset.class));

        userService.sendResetPasswordToken(userEmail);

        verify(passwordResetRepository, times(1)).save(any(PasswordReset.class));
        verify(emailService, times(1))
                .sendEmail(eq(userEmail), eq("Serwis społecznościowy - resetowanie hasła"), any());
    }

    @Test
    public void shouldResetPasswordNotLoggedUser() {
        ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
                .login("Jan123")
                .newPassword("newPassword")
                .repeatedNewPassword("newPassword")
                .build();
        String resetToken = "640a2ed9-1f5d-4920-a317-de49c78196d0";

        PasswordReset passwordReset = PasswordReset.builder()
                .passwordResetId(1L)
                .resetCode(resetToken)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusHours(10L))
                .user(user)
                .isUsed(false)
                .build();

        when(userRepository.findByUsernameOrEmail(resetPasswordDto.getLogin(), resetPasswordDto.getLogin()))
                .thenReturn(Optional.of(user));
        when(passwordResetRepository.findByUserAndResetCode(user, resetToken)).thenReturn(Optional.of(passwordReset));

        assertDoesNotThrow(() -> userService.resetPasswordNotLoggedUser(resetToken, resetPasswordDto));

        verify(passwordResetRepository, times(1)).save(passwordReset);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldGetAllUsers() {
        List<User> savedUsers = new ArrayList<>();
        User user2 = User.builder()
                .userId(2L)
                .username("Adam123")
                .password("Qwertyuiop")
                .email("adamNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .activityStatus(ActivityStatus.OFFLINE)
                .userProfile(UserProfile.builder()
                        .firstName("Adam")
                        .lastName("Nowak")
                        .gender(Gender.MALE)
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();
        savedUsers.add(user);
        savedUsers.add(user2);

        when(userRepository.findAll()).thenReturn(savedUsers);

        List<UserDto> resultUsers = userService.getAllUses();

        assertNotNull(resultUsers);
        assertEquals(2, resultUsers.size());
        assertEquals(userDtoListMapper.convert(savedUsers), resultUsers);
    }

    @Test
    public void shouldChangeActivityStatus() {
        String status = ActivityStatus.BE_RIGHT_BACK.toString();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        user.setActivityStatus(ActivityStatus.BE_RIGHT_BACK);

        userService.changeActivityStatus(status);

        verify(userRepository).save(user);
    }

    @Test
    public void shouldGetUserAccountsForAdmin() {
        int page = 1;
        int size = 2;

        List<User> savedUserAccounts = new ArrayList<>();
        User user2 = User.builder()
                .userId(2L)
                .username("Adam123")
                .password("Qwertyuiop")
                .email("adamNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .activityStatus(ActivityStatus.OFFLINE)
                .userProfile(UserProfile.builder()
                        .firstName("Adam")
                        .lastName("Nowak")
                        .gender(Gender.MALE)
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();
        savedUserAccounts.add(user);
        savedUserAccounts.add(user2);
        Pageable paging = PageRequest.of(page, size);
        Page<User> pageUsers = new PageImpl<User>(savedUserAccounts, paging, savedUserAccounts.size());

        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(userRepository.findByRolesNotContaining(any(Role.class), eq(paging))).thenReturn(pageUsers);

        UserAccountPageDto accountPageDto = userService.getUsersAccounts(page, size);

        assertNotNull(accountPageDto);
        assertEquals(userAccountDtoListMapper.convert(savedUserAccounts), accountPageDto.getUserAccounts());
        assertEquals(2, accountPageDto.getUserAccounts().size());
    }

    @Test
    public void shouldManageUserAccountSuccessfully() {
        Long userId = 1L;
        UserAccountUpdateDto userAccountUpdateDto = UserAccountUpdateDto.builder()
                .username("updatedUsername")
                .email("updatedEmail@gmail.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .isPublicProfile(true)
                .phoneNumber("111222333")
                .incorrectLoginCounter(0)
                .activateAccount(true)
                .isBlocked(false)
                .isBanned(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.manageUserAccount(userId, userAccountUpdateDto);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldDeleteUserByAdmin() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUserByAdmin(userId);

        verify(userRepository, times(1)).delete(user);
    }
}