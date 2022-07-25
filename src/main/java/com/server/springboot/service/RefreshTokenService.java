package com.server.springboot.service;

import com.server.springboot.domain.entity.RefreshToken;
import com.server.springboot.domain.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String username);

    void deleteByUser(User user);

    boolean checkExpirationDate(RefreshToken token);

    boolean existByUsername(String username);

    Optional<RefreshToken> findToken(String token);

}
