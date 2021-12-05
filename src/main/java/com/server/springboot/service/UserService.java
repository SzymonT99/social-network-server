package com.server.springboot.service;

import com.server.springboot.domain.dto.request.CreateUserDto;
import com.server.springboot.domain.dto.request.UserLoginDto;
import com.server.springboot.domain.dto.response.JwtResponse;

public interface UserService {

    void addUser(CreateUserDto createUserDto);

    void activateAccount(String token);

    JwtResponse loginUser(UserLoginDto userLoginDto);

}
