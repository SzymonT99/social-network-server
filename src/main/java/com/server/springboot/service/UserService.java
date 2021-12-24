package com.server.springboot.service;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.domain.dto.response.RefreshTokenResponse;

public interface UserService {

    void addUser(CreateUserDto createUserDto);

    void activateAccount(String token);

    JwtResponse loginUser(UserLoginDto userLoginDto);

    RefreshTokenResponse refreshExpiredToken(String refreshTokenStr);

    void logoutUser();

    void resendActivationLink(String userEmail);

    void deleteUser(DeleteUserDto deleteUserDto, boolean archive);

    JwtResponse changeUsername(ChangeUsernameDto changeUsernameDto);

    JwtResponse changeEmail(ChangeEmailDto changeEmailDto);

    void changePassword(ChangeUserPasswordDto changeUserPasswordDto);
}
