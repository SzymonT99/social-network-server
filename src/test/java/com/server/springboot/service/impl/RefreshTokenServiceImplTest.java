package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.response.PostDto;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.RefreshTokenRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.ResourceGoneException;
import com.server.springboot.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        user = User.builder()
                .userId(1L)
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .verifiedAccount(true)
                .activityStatus(ActivityStatus.OFFLINE)
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
    public void shouldCreateRefreshToken() {
        String username = "Jan123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.existsByUser(user)).thenReturn(false);
        when(jwtUtils.generateRefreshToken(username)).thenReturn(UUID.randomUUID().toString());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);

        assertNotNull(refreshToken);
        assertNotNull(refreshToken.getToken());
        assertEquals(user, refreshToken.getUser());
    }

    @Test
    public void shouldFindToken() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikphbj" +
                "EyMyIsImlhdCI6MTUxNjIzOTAyMn0.RHe9olPqW2BiehhUJz6QZ1lpUezlQXlpE6TwlezKHL0";
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .refreshTokenId(1L)
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusWeeks(1L))
                .build();

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(savedRefreshToken));

        Optional<RefreshToken> resultRefreshToken = refreshTokenService.findToken(token);

        assertNotNull(resultRefreshToken);
        assertEquals(savedRefreshToken, resultRefreshToken.get());
    }

    @Test
    public void shouldCheckExpirationDate() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikphbj" +
                "EyMyIsImlhdCI6MTUxNjIzOTAyMn0.RHe9olPqW2BiehhUJz6QZ1lpUezlQXlpE6TwlezKHL0";
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .refreshTokenId(1L)
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusWeeks(1L))
                .build();

        boolean result = refreshTokenService.checkExpirationDate(savedRefreshToken);

        assertTrue(result);
    }

    @Test
    public void shouldThrowErrorWhenCheckingDateIsExpired() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikphbj" +
                "EyMyIsImlhdCI6MTUxNjIzOTAyMn0.RHe9olPqW2BiehhUJz6QZ1lpUezlQXlpE6TwlezKHL0";
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .refreshTokenId(1L)
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().minusWeeks(1L))
                .build();

        doNothing().when(refreshTokenRepository).delete(savedRefreshToken);

        assertThatExceptionOfType(ResourceGoneException.class)
                .isThrownBy(() -> {
                    refreshTokenService.checkExpirationDate(savedRefreshToken);
                }).withMessage("RefreshToken has expired on " + savedRefreshToken.getExpiryDate());
        verify(refreshTokenRepository, times(1)).delete(savedRefreshToken);
    }

    @Test
    public void shouldExistTokenByUsername() {
        String username = "Jan123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.existsByUser(user)).thenReturn(true);

        boolean result = refreshTokenService.existByUsername(username);

        assertTrue(result);
    }

    @Test
    public void shouldDeleteTokenByUser() {
        doNothing().when(refreshTokenRepository).deleteByUser(user);

        refreshTokenService.deleteByUser(user);

        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }
}
