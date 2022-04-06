package com.server.springboot.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Optional;

import com.server.springboot.domain.entity.RefreshToken;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.repository.RefreshTokenRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.exception.ResourceGoneException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final Long refreshTokenExpirationMs = 86400000L;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Autowired
    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, JwtUtils jwtUtils) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Not found user with given username: " + username));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plus(refreshTokenExpirationMs, ChronoField.MILLI_OF_DAY.getBaseUnit()))
                .token(jwtUtils.generateRefreshToken(user.getUsername()))
                .build();
        if (refreshTokenRepository.existsByUser(user)) {
            refreshTokenRepository.deleteByUser(user);
        }
        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public Optional<RefreshToken> findToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean checkExpirationDate(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new ResourceGoneException("RefreshToken has expired on " + token.getExpiryDate());
        }
        return true;
    }

    @Override
    public boolean existByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Not found user with given username: " + username));
        return refreshTokenRepository.existsByUser(user);
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}