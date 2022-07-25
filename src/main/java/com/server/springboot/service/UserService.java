package com.server.springboot.service;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;

import java.util.List;

public interface UserService {

    void addUser(CreateUserDto createUserDto);

    ActivatedAccountDto activateAccount(String token);

    JwtResponse loginUser(UserLoginDto userLoginDto);

    RefreshTokenResponse refreshExpiredToken(String refreshTokenStr);

    void logoutUser(Long userId);

    void resendActivationLink(String userEmail);

    void deleteUser(DeleteUserDto deleteUserDto);

    JwtResponse changeUsername(Long userId, ChangeUsernameDto changeUsernameDto);

    JwtResponse changeEmail(Long userId, ChangeEmailDto changeEmailDto);

    JwtResponse changePassword(Long userId, ChangeUserPasswordDto changeUserPasswordDto);

    void changePhoneNumber(Long userId, ChangePhoneNumberDto changePhoneNumberDto);

    void reportUserBySuspectId(RequestReportDto requestReportDto);

    void decideAboutReport(Long reportId, boolean confirmation);

    List<ReportDto> getAllUserReports();

    void sendResetPasswordToken(String userEmail);

    void resetPasswordNotLoggedUser(String token, ResetPasswordDto resetPasswordDto);

    List<UserDto> getAllUses();

    void changeActivityStatus(String status);

    UserAccountPageDto getUsersAccounts(Integer page, Integer size);

    void manageUserAccount(Long userId, UserAccountUpdateDto userAccountUpdateDto);

    void deleteUserByAdmin(Long userId);
}
