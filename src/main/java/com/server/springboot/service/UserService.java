package com.server.springboot.service;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.JwtResponse;
import com.server.springboot.domain.dto.response.RefreshTokenResponse;
import com.server.springboot.domain.dto.response.ReportDto;

import java.util.List;

public interface UserService {

    void addUser(CreateUserDto createUserDto);

    void activateAccount(String token);

    JwtResponse loginUser(UserLoginDto userLoginDto);

    RefreshTokenResponse refreshExpiredToken(String refreshTokenStr);

    void logoutUser();

    void resendActivationLink(String userEmail);

    void deleteUser(DeleteUserDto deleteUserDto, boolean archive);

    JwtResponse changeUsername(Long userId, ChangeUsernameDto changeUsernameDto);

    JwtResponse changeEmail(Long userId, ChangeEmailDto changeEmailDto);

    JwtResponse changePassword(Long userId, ChangeUserPasswordDto changeUserPasswordDto);

    void changePhoneNumber(Long userId, ChangePhoneNumberDto changePhoneNumberDto);

    void reportUserBySuspectId(RequestReportDto requestReportDto);

    void decideAboutReport(Long reportId, boolean confirmation);

    List<ReportDto> getAllUserReports();
}
